package com.example.grotrack;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grotrack.data.GroTrackDatabase;
import com.example.grotrack.data.GroceryBatch;
import com.example.grotrack.data.InventoryItem;
import com.example.grotrack.data.Product;
import com.example.grotrack.data.RoomTransaction;
import com.example.grotrack.data.ShoppingListItem;
import com.example.grotrack.data.StockHistory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoppingListActivity extends AppCompatActivity {
    private static final String[] UNITS={"piece","kg","g","litre","ml"};
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private ShoppingListAdapter adapter;
    private TextView empty;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_shopping_list);
        database=GroTrackDatabase.getInstance(getApplicationContext()); empty=findViewById(R.id.shoppingEmptyMessage);
        RecyclerView list=findViewById(R.id.shoppingList); list.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ShoppingListAdapter(new ShoppingListAdapter.Actions() {
            @Override public void purchase(ShoppingListItem item) { showPurchaseDialog(item); }
            @Override public void edit(ShoppingListItem item) { showItemDialog(item); }
            @Override public void delete(ShoppingListItem item) { confirmDelete(item); }
        }); list.setAdapter(adapter);
        findViewById(R.id.addShoppingItemButton).setOnClickListener(v -> showItemDialog(null));
        findViewById(R.id.suggestLowStockButton).setOnClickListener(v -> addLowStockSuggestions());
        findViewById(R.id.backToInventoryButton).setOnClickListener(v -> finish());
        findViewById(R.id.openShoppingWasteHistoryButton).setOnClickListener(v -> startActivity(new Intent(this,WasteHistoryActivity.class)));
        loadItems();
    }

    private void loadItems() {
        executor.execute(() -> { List<ShoppingListItem> items=database.shoppingListDao().getAll(); runOnUiThread(() -> { adapter.submit(items); empty.setVisibility(items.isEmpty()?View.VISIBLE:View.GONE); }); });
    }

    private void showItemDialog(ShoppingListItem editing) {
        View form=LayoutInflater.from(this).inflate(R.layout.dialog_shopping_item,null);
        EditText name=form.findViewById(R.id.shoppingNameInput), quantity=form.findViewById(R.id.shoppingQuantityInput);
        Spinner unit=form.findViewById(R.id.shoppingUnitInput);
        unit.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,UNITS));
        if(editing!=null) { name.setText(editing.itemName); quantity.setText(number(editing.quantity)); for(int i=0;i<UNITS.length;i++)if(UNITS[i].equals(editing.unit))unit.setSelection(i); }
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(editing==null?"Add shopping item":"Edit shopping item").setView(form).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title=name.getText().toString().trim(); Double amount=parsePositive(quantity,"Quantity");
            if(title.isEmpty()) { name.setError("Enter an item name"); return; } if(amount==null)return;
            String selectedUnit=unit.getSelectedItem().toString();
            executor.execute(() -> {
                ShoppingListItem item=editing==null?new ShoppingListItem():database.shoppingListDao().getById(editing.id);
                if(item!=null&&!item.purchased) {
                    Product linked=database.productDao().findActiveByNameAndUnit(title,selectedUnit);
                    item.itemName=title; item.quantity=amount; item.unit=selectedUnit; item.productId=linked==null?null:linked.id;
                    if(editing==null) { item.createdAt=System.currentTimeMillis(); database.shoppingListDao().insert(item); } else database.shoppingListDao().update(item);
                }
                runOnUiThread(() -> { dialog.dismiss(); loadItems(); toast("Shopping item saved"); });
            });
        })); dialog.show();
    }

    private void addLowStockSuggestions() {
        executor.execute(() -> {
            List<Product> products=database.productDao().getAll(); List<InventoryItem> rows=database.batchDao().getInventory(); List<ShoppingListItem> list=database.shoppingListDao().getAll();
            Map<Long,Double> totals=new HashMap<>(); for(InventoryItem row:rows) if(!row.archived) totals.put(row.productId,totals.getOrDefault(row.productId,0d)+row.remainingQuantity);
            int added=0;
            for(Product product:products) {
                if(product.archived)continue;
                double deficit=product.minimumStock-totals.getOrDefault(product.id,0d);
                if(deficit<=0)continue;
                boolean already=false; for(ShoppingListItem listed:list) if(!listed.purchased&&listed.productId!=null&&listed.productId==product.id) { already=true; break; }
                if(!already) { ShoppingListItem suggestion=new ShoppingListItem(); suggestion.productId=product.id; suggestion.itemName=product.name; suggestion.quantity=deficit; suggestion.unit=product.unit; suggestion.createdAt=System.currentTimeMillis(); database.shoppingListDao().insert(suggestion); added++; }
            }
            int count=added; runOnUiThread(() -> { loadItems(); toast(count==0?"No new low-stock suggestions":"Added "+count+" low-stock suggestion"+(count==1?"":"s")); });
        });
    }

    private void showPurchaseDialog(ShoppingListItem listed) {
        View form=LayoutInflater.from(this).inflate(R.layout.dialog_batch,null);
        EditText quantity=form.findViewById(R.id.batchQuantityInput), price=form.findViewById(R.id.batchPriceInput);
        Button purchaseDate=form.findViewById(R.id.batchPurchaseDateButton), expiryDate=form.findViewById(R.id.batchExpiryDateButton);
        quantity.setText(number(listed.quantity));
        LocalDate[] dates={LocalDate.now(),LocalDate.now().plusDays(7)};
        setDateButton(purchaseDate,dates,0,"Purchase date"); setDateButton(expiryDate,dates,1,"Expiry date");
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Record purchase: "+listed.itemName).setMessage("Inventory stock changes only when this purchase is saved.").setView(form).setNegativeButton("Cancel",null).setPositiveButton("Save purchase",null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Double amount=parsePositive(quantity,"Purchased quantity"); if(amount==null)return;
            Double cost=parseOptionalPrice(price); if(cost==INVALID)return;
            executor.execute(() -> {
                String error=null;
                try {
                    RoomTransaction.run(database, () -> {
                        ShoppingListItem fresh=database.shoppingListDao().getById(listed.id);
                        if(fresh==null||fresh.purchased)throw new IllegalStateException("This shopping item was already purchased or removed.");
                        Product product=fresh.productId==null?database.productDao().findActiveByNameAndUnit(fresh.itemName,fresh.unit):database.productDao().getById(fresh.productId);
                        if(product==null||product.archived) { product=new Product(); product.name=fresh.itemName; product.category="Other"; product.unit=fresh.unit; product.minimumStock=0; product.id=database.productDao().insert(product); }
                        GroceryBatch batch=new GroceryBatch(); batch.productId=product.id; batch.purchasedQuantity=amount; batch.remainingQuantity=amount;
                        batch.purchaseDate=dates[0].format(DateTimeFormatter.ISO_LOCAL_DATE); batch.expiryDate=dates[1].format(DateTimeFormatter.ISO_LOCAL_DATE); batch.purchasePrice=cost;
                        long batchId=database.batchDao().insert(batch);
                        StockHistory history=new StockHistory(); history.productId=product.id; history.batchId=batchId; history.productName=product.name; history.unit=product.unit; history.actionType="PURCHASE"; history.quantity=amount; history.timestamp=System.currentTimeMillis(); database.stockHistoryDao().insert(history);
                        fresh.purchased=true; database.shoppingListDao().update(fresh);
                    });
                } catch(Exception ex) { error=ex.getMessage()==null?"Purchase could not be recorded.":ex.getMessage(); }
                String result=error; runOnUiThread(() -> { if(result==null) { dialog.dismiss(); loadItems(); toast("Purchase recorded and inventory batch created"); } else toast(result); });
            });
        })); dialog.show();
    }

    private void confirmDelete(ShoppingListItem item) {
        new AlertDialog.Builder(this).setTitle("Remove "+item.itemName+"?").setMessage("This removes the shopping-list entry. Any recorded purchase remains in inventory and history.").setNegativeButton("Cancel",null).setPositiveButton("Remove",(d,w)->executor.execute(()->{ ShoppingListItem fresh=database.shoppingListDao().getById(item.id); if(fresh!=null)database.shoppingListDao().delete(fresh); runOnUiThread(this::loadItems); })).show();
    }

    private void setDateButton(Button button,LocalDate[] dates,int index,String title) {
        updateDateButton(button,dates[index],title);
        button.setOnClickListener(v->{ LocalDate date=dates[index]; new DatePickerDialog(this,(picker,year,month,day)->{ dates[index]=LocalDate.of(year,month+1,day); updateDateButton(button,dates[index],title); },date.getYear(),date.getMonthValue()-1,date.getDayOfMonth()).show(); });
    }
    private void updateDateButton(Button button,LocalDate date,String title) { button.setText(title+": "+date.format(DateTimeFormatter.ISO_LOCAL_DATE)); }
    private Double parsePositive(EditText field,String label) { try { double value=Double.parseDouble(field.getText().toString().trim()); if(!Double.isFinite(value)||value<=0)throw new NumberFormatException(); return value; } catch(Exception ex) { field.setError(label+" must be greater than zero"); return null; } }
    private static final Double INVALID=Double.NEGATIVE_INFINITY;
    private Double parseOptionalPrice(EditText field) { String text=field.getText().toString().trim(); if(text.isEmpty())return null; try { double value=Double.parseDouble(text); if(!Double.isFinite(value)||value<0)throw new NumberFormatException(); return value; } catch(Exception ex) { field.setError("Price must be zero or more"); return INVALID; } }
    private String number(double value) { return value==Math.rint(value)?String.format(Locale.ROOT,"%.0f",value):Double.toString(value); }
    private void toast(String message) { Toast.makeText(this,message,Toast.LENGTH_SHORT).show(); }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
