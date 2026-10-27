package com.example.grotrack;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grotrack.data.GroTrackDatabase;
import com.example.grotrack.data.HistoryItem;
import com.example.grotrack.data.Product;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WasteHistoryActivity extends AppCompatActivity {
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private WasteHistoryAdapter adapter;
    private Spinner productFilter;
    private TextView empty;
    private Button fromButton,toButton;
    private LocalDate fromDate,toDate;
    private List<ProductOption> productOptions=new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_waste_history); database=GroTrackDatabase.getInstance(getApplicationContext());
        productFilter=findViewById(R.id.wasteProductFilter); empty=findViewById(R.id.wasteEmptyMessage); fromButton=findViewById(R.id.wasteFromDateButton); toButton=findViewById(R.id.wasteToDateButton);
        RecyclerView list=findViewById(R.id.wasteHistoryList); list.setLayoutManager(new LinearLayoutManager(this)); adapter=new WasteHistoryAdapter(); list.setAdapter(adapter);
        fromButton.setOnClickListener(v->chooseDate(true)); toButton.setOnClickListener(v->chooseDate(false));
        findViewById(R.id.clearWasteFiltersButton).setOnClickListener(v->{ fromDate=null; toDate=null; updateDateLabels(); if(productFilter.getSelectedItemPosition()!=0)productFilter.setSelection(0); else loadHistory(); });
        findViewById(R.id.backFromWasteButton).setOnClickListener(v->finish());
        executor.execute(()->{ List<Product> products=database.productDao().getAll(); runOnUiThread(()->setupProductFilter(products)); });
    }

    private void setupProductFilter(List<Product> products) {
        productOptions.clear(); productOptions.add(new ProductOption(null,"All products"));
        for(Product product:products) productOptions.add(new ProductOption(product.id,product.name));
        ArrayAdapter<ProductOption> spinnerAdapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,productOptions);
        productFilter.setAdapter(spinnerAdapter);
        productFilter.setOnItemSelectedListener(new SimpleSelectionListener(this::loadHistory));
        loadHistory();
    }

    private void chooseDate(boolean isFrom) {
        LocalDate initial=isFrom?(fromDate==null?LocalDate.now():fromDate):(toDate==null?LocalDate.now():toDate);
        new DatePickerDialog(this,(picker,year,month,day)->{
            LocalDate chosen=LocalDate.of(year,month+1,day);
            LocalDate newFrom=isFrom?chosen:fromDate, newTo=isFrom?toDate:chosen;
            if(newFrom!=null&&newTo!=null&&newFrom.isAfter(newTo)) { Toast.makeText(this,"Start date must not be after end date",Toast.LENGTH_SHORT).show(); return; }
            if(isFrom)fromDate=chosen; else toDate=chosen; updateDateLabels(); loadHistory();
        },initial.getYear(),initial.getMonthValue()-1,initial.getDayOfMonth()).show();
    }

    private void updateDateLabels() {
        DateTimeFormatter format=DateTimeFormatter.ISO_LOCAL_DATE;
        fromButton.setText("From: "+(fromDate==null?"all dates":fromDate.format(format)));
        toButton.setText("To: "+(toDate==null?"all dates":toDate.format(format)));
    }

    private void loadHistory() {
        if(productOptions.isEmpty())return;
        long start=fromDate==null?0:fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end=toDate==null?0:toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Object selected=productFilter.getSelectedItem(); Long productId=selected instanceof ProductOption?((ProductOption)selected).id:null;
        executor.execute(()->{ List<HistoryItem> data=database.stockHistoryDao().getWasteHistory(start,end,productId); runOnUiThread(()->{ adapter.submit(data); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); }); });
    }

    private static class ProductOption {
        final Long id; final String title;
        ProductOption(Long id,String title) { this.id=id; this.title=title; }
        @Override public String toString() { return title; }
    }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
