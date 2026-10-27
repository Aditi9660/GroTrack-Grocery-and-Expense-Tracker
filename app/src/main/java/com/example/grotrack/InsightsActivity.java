package com.example.grotrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.grotrack.data.GroTrackDatabase;
import com.example.grotrack.data.HistoryItem;
import com.example.grotrack.data.InventoryItem;
import com.example.grotrack.data.Product;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InsightsActivity extends AppCompatActivity {
    private static final String[] PERIODS={"Last 7 days","Last 30 days","Last 90 days","All time"};
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private Spinner periodFilter;
    private TextView inventorySummary,wasteRecordCount,wasteQuantity,topWasted;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_insights); database=GroTrackDatabase.getInstance(getApplicationContext());
        inventorySummary=findViewById(R.id.insightInventorySummary); wasteRecordCount=findViewById(R.id.insightWasteRecordCount); wasteQuantity=findViewById(R.id.insightWasteQuantity); topWasted=findViewById(R.id.insightTopWasted);
        periodFilter=findViewById(R.id.insightPeriodFilter); periodFilter.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,PERIODS));
        periodFilter.setOnItemSelectedListener(new SimpleSelectionListener(this::loadInsights));
        findViewById(R.id.insightWasteHistoryButton).setOnClickListener(v->startActivity(new Intent(this,WasteHistoryActivity.class)));
        findViewById(R.id.insightBackButton).setOnClickListener(v->finish());
    }

    @Override protected void onResume() { super.onResume(); loadInsights(); }

    private void loadInsights() {
        int selected=periodFilter==null?1:periodFilter.getSelectedItemPosition();
        LocalDate today=LocalDate.now();
        long from=selected==3?0:today.minusDays(daysFor(selected)-1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long to=today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        executor.execute(()->{
            List<Product> products=database.productDao().getAll();
            List<InventoryItem> batches=database.batchDao().getInventory();
            List<HistoryItem> waste=database.stockHistoryDao().getWasteHistory(from,to,null);
            Summary summary=summarize(products,batches,waste);
            runOnUiThread(()->display(summary));
        });
    }

    private int daysFor(int selected) { return selected==0?7:selected==2?90:30; }

    private Summary summarize(List<Product> products,List<InventoryItem> batches,List<HistoryItem> waste) {
        int expiring=0,expired=0,low=0,activeProducts=0;
        Map<Long,Double> quantities=new HashMap<>();
        for(Product product:products)if(!product.archived)activeProducts++;
        for(InventoryItem batch:batches) {
            if(batch.archived)continue;
            quantities.put(batch.productId,quantities.getOrDefault(batch.productId,0d)+batch.remainingQuantity);
            if(batch.remainingQuantity<=0)continue;
            try { long days=ChronoUnit.DAYS.between(LocalDate.now(),LocalDate.parse(batch.expiryDate)); if(days<0)expired++; else if(days<=3)expiring++; } catch(Exception ignored) { }
        }
        for(Product product:products)if(!product.archived&&quantities.getOrDefault(product.id,0d)<product.minimumStock)low++;
        Map<String,BigDecimal> units=new TreeMap<>(); Map<String,WasteStat> productsWasted=new HashMap<>();
        for(HistoryItem item:waste) {
            String unit=item.unit==null?"unit not set":item.unit;
            BigDecimal quantity=BigDecimal.valueOf(item.quantity);
            units.put(unit,units.getOrDefault(unit,BigDecimal.ZERO).add(quantity));
            String name=item.productName==null?"Removed product":item.productName;
            String key=name.toLowerCase(Locale.ROOT)+"\u0000"+unit.toLowerCase(Locale.ROOT);
            WasteStat stat=productsWasted.get(key); if(stat==null) { stat=new WasteStat(name,unit); productsWasted.put(key,stat); }
            stat.count++; stat.quantity=stat.quantity.add(quantity);
        }
        List<WasteStat> ranked=new ArrayList<>(productsWasted.values());
        ranked.sort((left,right)->{ int countOrder=Integer.compare(right.count,left.count); if(countOrder!=0)return countOrder; int quantityOrder=right.quantity.compareTo(left.quantity); return quantityOrder!=0?quantityOrder:left.name.compareToIgnoreCase(right.name); });
        return new Summary(activeProducts,expiring,expired,low,waste.size(),units,ranked);
    }

    private void display(Summary summary) {
        inventorySummary.setText("Products: "+summary.products+"\nBatches expiring within 3 days: "+summary.expiring+"\nExpired batches with stock: "+summary.expired+"\nProducts below minimum stock: "+summary.low);
        wasteRecordCount.setText("Waste records in selected period: "+summary.recordCount);
        if(summary.units.isEmpty())wasteQuantity.setText("No waste recorded in this period.");
        else { StringBuilder text=new StringBuilder(); for(Map.Entry<String,BigDecimal> entry:summary.units.entrySet())text.append(format(entry.getValue())).append(" ").append(entry.getKey()).append('\n'); wasteQuantity.setText(text.toString().trim()); }
        if(summary.ranked.isEmpty())topWasted.setText("No waste recorded in this period.");
        else { StringBuilder text=new StringBuilder(); int limit=Math.min(5,summary.ranked.size()); for(int i=0;i<limit;i++) { WasteStat stat=summary.ranked.get(i); text.append(i+1).append(". ").append(stat.name).append(" — ").append(stat.count).append(" record").append(stat.count==1?"":"s").append(" (total ").append(format(stat.quantity)).append(" ").append(stat.unit).append(")\n"); } topWasted.setText(text.toString().trim()); }
    }

    private String format(BigDecimal value) { return value.stripTrailingZeros().toPlainString(); }
    private static class WasteStat { final String name,unit; int count; BigDecimal quantity=BigDecimal.ZERO; WasteStat(String name,String unit){this.name=name;this.unit=unit;} }
    private static class Summary { final int products,expiring,expired,low,recordCount; final Map<String,BigDecimal> units; final List<WasteStat> ranked; Summary(int p,int e,int x,int l,int r,Map<String,BigDecimal> u,List<WasteStat> sorted){products=p;expiring=e;expired=x;low=l;recordCount=r;units=u;ranked=sorted;} }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
