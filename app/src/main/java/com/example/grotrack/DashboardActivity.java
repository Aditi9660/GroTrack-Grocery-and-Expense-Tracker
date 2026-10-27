package com.example.grotrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import com.example.grotrack.data.GroTrackDatabase;
import com.example.grotrack.data.HistoryItem;
import com.example.grotrack.data.InventoryItem;
import com.example.grotrack.data.Product;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private TextView productsCount,expiringCount,lowStockCount,inventoryDetails,wasteSummary;
    private LinearLayout expiringList;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override protected void onCreate(Bundle state) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(state);
        // Keep content below system bars on Android versions enforcing edge-to-edge.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_dashboard);
        database=GroTrackDatabase.getInstance(getApplicationContext());
        ExpiryWorkScheduler.schedulePeriodic(getApplicationContext());
        productsCount=findViewById(R.id.homeProductCount); expiringCount=findViewById(R.id.homeExpiringCount); lowStockCount=findViewById(R.id.homeLowStockCount);
        inventoryDetails=findViewById(R.id.homeInventoryDetails); expiringList=findViewById(R.id.homeExpiringList); wasteSummary=findViewById(R.id.homeWasteSummary);
        notificationPermissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
            ExpiryNotifications.createChannel(this);
            Toast.makeText(this,granted?"Expiry notifications enabled":"Notifications are off. GroTrack will keep tracking expiry dates.",Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.navInventoryCard).setOnClickListener(v->startActivity(new Intent(this,MainActivity.class)));
        findViewById(R.id.navShoppingCard).setOnClickListener(v->startActivity(new Intent(this,ShoppingListActivity.class)));
        findViewById(R.id.navWasteCard).setOnClickListener(v->startActivity(new Intent(this,WasteHistoryActivity.class)));
        findViewById(R.id.navInsightsCard).setOnClickListener(v->startActivity(new Intent(this,InsightsActivity.class)));
        findViewById(R.id.homeAddItemButton).setOnClickListener(v->startActivity(new Intent(this,MainActivity.class).putExtra(MainActivity.EXTRA_ADD_PRODUCT,true)));
        findViewById(R.id.homeEnableNotificationsButton).setOnClickListener(v->enableNotifications());
        findViewById(R.id.homeTestExpiryButton).setOnClickListener(v->testExpiryCheck());
    }

    @Override protected void onResume() { super.onResume(); loadDashboard(); }

    private void loadDashboard() {
        executor.execute(()->{
            LocalDate today=LocalDate.now();
            List<Product> products=database.productDao().getAll();
            List<InventoryItem> batches=database.batchDao().getInventory();
            long weekStart=today.minusDays(6).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            long tomorrow=today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<HistoryItem> waste=database.stockHistoryDao().getWasteHistory(weekStart,tomorrow,null);
            DashboardSummary summary=calculate(products,batches,waste,today);
            runOnUiThread(()->render(summary));
        });
    }

    private DashboardSummary calculate(List<Product> products,List<InventoryItem> batches,List<HistoryItem> waste,LocalDate today) {
        int activeProducts=0,expiring=0,expired=0,low=0;
        Map<Long,Double> stockByProduct=new HashMap<>();
        List<InventoryItem> soon=new ArrayList<>();
        for(Product product:products)if(!product.archived)activeProducts++;
        for(InventoryItem batch:batches) {
            if(batch.archived)continue;
            stockByProduct.put(batch.productId,stockByProduct.getOrDefault(batch.productId,0d)+batch.remainingQuantity);
            if(batch.remainingQuantity<=0||batch.expiryDate==null)continue;
            try {
                long days=ChronoUnit.DAYS.between(today,LocalDate.parse(batch.expiryDate));
                if(days<0)expired++;
                else if(days<=3) { expiring++; soon.add(batch); }
            } catch(Exception ignored) { }
        }
        for(Product product:products)if(!product.archived&&stockByProduct.getOrDefault(product.id,0d)<product.minimumStock)low++;
        soon.sort(Comparator.comparing(item->item.expiryDate));
        Map<String,BigDecimal> wasteByUnit=new TreeMap<>();
        for(HistoryItem item:waste) {
            String unit=item.unit==null?"unit not set":item.unit;
            wasteByUnit.put(unit,wasteByUnit.getOrDefault(unit,BigDecimal.ZERO).add(BigDecimal.valueOf(item.quantity)));
        }
        return new DashboardSummary(activeProducts,expiring,expired,low,soon,waste.size(),wasteByUnit);
    }

    private void render(DashboardSummary summary) {
        productsCount.setText(String.valueOf(summary.products)); expiringCount.setText(String.valueOf(summary.expiring)); lowStockCount.setText(String.valueOf(summary.low));
        inventoryDetails.setText("Expired batches with stock: "+summary.expired+"\nProducts below minimum stock: "+summary.low);
        expiringList.removeAllViews();
        if(summary.soon.isEmpty()) {
            TextView empty=makePreviewText("Nothing is due to expire within 3 days.",false); expiringList.addView(empty);
        } else {
            int count=Math.min(4,summary.soon.size());
            for(int i=0;i<count;i++) {
                InventoryItem item=summary.soon.get(i);
                String line=item.name+"  •  "+format(item.remainingQuantity)+" "+item.unit+"\nExpires "+LocalDate.parse(item.expiryDate).format(DateTimeFormatter.ofPattern("d MMM",Locale.getDefault()));
                expiringList.addView(makePreviewText(line,true));
            }
            if(summary.soon.size()>count)expiringList.addView(makePreviewText("+ "+(summary.soon.size()-count)+" more batches",false));
        }
        if(summary.wasteRecords==0)wasteSummary.setText("Food waste • Last 7 days\nNo waste has been recorded.");
        else {
            StringBuilder message=new StringBuilder("Food waste • Last 7 days\n").append(summary.wasteRecords).append(" record").append(summary.wasteRecords==1?"":"s");
            for(Map.Entry<String,BigDecimal> entry:summary.wasteByUnit.entrySet())message.append("\n").append(entry.getValue().stripTrailingZeros().toPlainString()).append(" ").append(entry.getKey());
            wasteSummary.setText(message.toString());
        }
    }

    private TextView makePreviewText(String text,boolean card) {
        TextView view=new TextView(this); view.setText(text); view.setTextColor(getColor(card?R.color.grotrack_text:R.color.grotrack_secondary)); view.setTextSize(card?14:13); view.setPadding(14,12,14,12);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT); params.bottomMargin=8; view.setLayoutParams(params);
        if(card)view.setBackgroundResource(R.drawable.bg_grocery_card); else view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private void enableNotifications() {
        if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        else {
            ExpiryNotifications.createChannel(this);
            Toast.makeText(this,ExpiryNotifications.canNotify(this)?"Expiry notifications are enabled":"Notifications are disabled in system settings.",Toast.LENGTH_LONG).show();
        }
    }

    private void testExpiryCheck() {
        if(!ExpiryNotifications.canNotify(this)) { Toast.makeText(this,"Enable notifications first to receive the test alert.",Toast.LENGTH_LONG).show(); return; }
        ExpiryWorkScheduler.runTestNow(getApplicationContext());
        Toast.makeText(this,"Expiry check queued. A notification will confirm it ran.",Toast.LENGTH_SHORT).show();
    }

    private String format(double value) { return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString(); }
    private static final class DashboardSummary {
        final int products,expiring,expired,low,wasteRecords; final List<InventoryItem> soon; final Map<String,BigDecimal> wasteByUnit;
        DashboardSummary(int products,int expiring,int expired,int low,List<InventoryItem> soon,int records,Map<String,BigDecimal> wasteByUnit) { this.products=products;this.expiring=expiring;this.expired=expired;this.low=low;this.soon=soon;this.wasteRecords=records;this.wasteByUnit=wasteByUnit; }
    }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
