package com.example.grotrack;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.grotrack.data.GroTrackDatabase;
import com.example.grotrack.data.GroceryBatch;
import com.example.grotrack.data.InventoryItem;
import com.example.grotrack.data.Product;
import com.example.grotrack.data.RoomTransaction;
import com.example.grotrack.data.StockHistory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_ADD_PRODUCT="com.example.grotrack.ADD_PRODUCT";
    private static final String[] CATEGORIES = {"All categories", "Dairy", "Vegetables", "Fruits", "Grains", "Packaged Food", "Other"};
    private static final String[] PRODUCT_CATEGORIES = {"Dairy", "Vegetables", "Fruits", "Grains", "Packaged Food", "Other"};
    private static final String[] UNITS = {"piece", "kg", "g", "litre", "ml"};
    private static final String[] STATUSES = {"All statuses", "Fresh", "Expiring Soon", "Expired"};
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private InventoryAdapter adapter;
    private final List<InventoryItem> inventory = new ArrayList<>();
    private EditText searchInput;
    private ChipGroup categoryFilterGroup;
    private Spinner statusFilter;
    private TextView emptyMessage, productsSummary, expiringSummary, otherSummary;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = GroTrackDatabase.getInstance(getApplicationContext());
        RecyclerView list = findViewById(R.id.inventoryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(new InventoryAdapter.Actions() {
            @Override public void addBatch(InventoryItem item) { showBatchDialog(item.productId, item.name, item.unit); }
            @Override public void edit(InventoryItem item) { loadProductForEdit(item.productId); }
            @Override public void delete(InventoryItem item) { confirmDelete(item); }
            @Override public void consume(InventoryItem item) { showTransactionDialog(item, false); }
            @Override public void waste(InventoryItem item) { showTransactionDialog(item, true); }
        });
        list.setAdapter(adapter);
        searchInput = findViewById(R.id.searchInput);
        categoryFilterGroup = findViewById(R.id.categoryFilterGroup);
        statusFilter = findViewById(R.id.statusFilter);
        emptyMessage = findViewById(R.id.emptyMessage);
        productsSummary = findViewById(R.id.dashboardProducts);
        expiringSummary = findViewById(R.id.dashboardExpiring);
        otherSummary = findViewById(R.id.dashboardExpired);
        setupCategoryFilters();
        setupSpinner(statusFilter, STATUSES);
        findViewById(R.id.addProductButton).setOnClickListener(v -> showProductDialog(null));
        findViewById(R.id.inventoryHomeButton).setOnClickListener(v -> finish());
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        categoryFilterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
        statusFilter.setOnItemSelectedListener(new SimpleSelectionListener(this::applyFilters));
        if(getIntent().getBooleanExtra(EXTRA_ADD_PRODUCT,false))findViewById(R.id.addProductButton).post(()->showProductDialog(null));
    }

    @Override protected void onResume() { super.onResume(); loadInventory(); }

    private void setupSpinner(Spinner spinner, String[] choices) {
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices));
    }

    private void setupCategoryFilters() {
        for(String choice:CATEGORIES) {
            Chip chip=new Chip(this);
            chip.setText(choice); chip.setCheckable(true); chip.setId(View.generateViewId());
            categoryFilterGroup.addView(chip);
            if(CATEGORIES[0].equals(choice))chip.setChecked(true);
        }
    }

    private void loadInventory() {
        databaseExecutor.execute(() -> {
            List<InventoryItem> rows = database.batchDao().getInventory();
            List<Product> products = database.productDao().getAll();
            List<Product> activeProducts = new ArrayList<>();
            for (Product p : products) if (!p.archived) activeProducts.add(p);
            runOnUiThread(() -> {
                inventory.clear(); inventory.addAll(rows);
                applyFilters();
                updateDashboard(activeProducts, rows);
            });
        });
    }

    private void applyFilters() {
        String query = searchInput == null ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        View selectedCategory=categoryFilterGroup==null?null:categoryFilterGroup.findViewById(categoryFilterGroup.getCheckedChipId());
        String category=selectedCategory instanceof Chip?((Chip)selectedCategory).getText().toString():CATEGORIES[0];
        String filterStatus = statusFilter == null || statusFilter.getSelectedItem() == null ? STATUSES[0] : statusFilter.getSelectedItem().toString();
        List<InventoryItem> filtered = new ArrayList<>();
        for (InventoryItem item : inventory) {
            if (item.archived || !item.name.toLowerCase(Locale.ROOT).contains(query)) continue;
            if (!CATEGORIES[0].equals(category) && !category.equals(item.category)) continue;
            if (!STATUSES[0].equals(filterStatus) && !filterStatus.equals(status(item.expiryDate))) continue;
            filtered.add(item);
        }
        if (adapter != null) adapter.submit(filtered);
        if (emptyMessage != null) {
            emptyMessage.setText(inventory.isEmpty() ? "No groceries yet. Add a product to get started." : "No products match these search and filter settings.");
            emptyMessage.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateDashboard(List<Product> products, List<InventoryItem> rows) {
        int expiring = 0, expired = 0;
        Map<Long, Double> totalByProduct = new HashMap<>();
        Set<Long> seenActive = new HashSet<>();
        for (Product p : products) seenActive.add(p.id);
        for (InventoryItem row : rows) {
            if (row.archived) continue;
            totalByProduct.put(row.productId, totalByProduct.getOrDefault(row.productId, 0d) + row.remainingQuantity);
            if (row.remainingQuantity > 0) {
                if ("Expiring Soon".equals(status(row.expiryDate))) expiring++;
                if ("Expired".equals(status(row.expiryDate))) expired++;
            }
        }
        int lowStock = 0;
        for (Product product : products) {
            if (!product.archived && totalByProduct.getOrDefault(product.id, 0d) < product.minimumStock) lowStock++;
        }
        productsSummary.setText("Products\n" + seenActive.size());
        expiringSummary.setText("Near expiry\n" + expiring + " batches");
        otherSummary.setText("Expired with stock: " + expired + "   •   Below minimum: " + lowStock);
    }

    private String status(String expiry) {
        try {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(expiry));
            return days < 0 ? "Expired" : days <= 3 ? "Expiring Soon" : "Fresh";
        } catch (Exception ignored) { return "Date unavailable"; }
    }

    private void showProductDialog(Product existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
        EditText name = form.findViewById(R.id.productNameInput), minimum = form.findViewById(R.id.minimumStockInput);
        EditText quantity = form.findViewById(R.id.quantityInput), price = form.findViewById(R.id.priceInput);
        Spinner category = form.findViewById(R.id.productCategoryInput), unit = form.findViewById(R.id.productUnitInput);
        setupSpinner(category, PRODUCT_CATEGORIES); setupSpinner(unit, UNITS);
        Button purchaseDate = form.findViewById(R.id.purchaseDateButton), expiryDate = form.findViewById(R.id.expiryDateButton);
        LocalDate[] dates = {LocalDate.now(), LocalDate.now().plusDays(7)};
        setDateButton(purchaseDate, dates, 0, "Purchase date"); setDateButton(expiryDate, dates, 1, "Expiry date");
        if (existing != null) {
            name.setText(existing.name); minimum.setText(number(existing.minimumStock));
            category.setSelection(indexOf(PRODUCT_CATEGORIES, existing.category)); unit.setSelection(indexOf(UNITS, existing.unit));
            // Batch quantities are expressed in the product's unit, so changing it would relabel existing stock.
            unit.setEnabled(false);
            form.findViewById(R.id.batchHeading).setVisibility(View.GONE);
            quantity.setVisibility(View.GONE); price.setVisibility(View.GONE); purchaseDate.setVisibility(View.GONE); expiryDate.setVisibility(View.GONE);
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(existing == null ? "Add grocery product" : "Edit product")
                .setView(form).setNegativeButton("Cancel", null).setPositiveButton(existing == null ? "Save product" : "Save changes", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String productName = name.getText().toString().trim();
            Double min = parseNonNegative(minimum, "Minimum stock");
            if (productName.isEmpty()) { name.setError("Enter a product name"); return; }
            if (min == null) return;
            final String selectedCategory = category.getSelectedItem().toString(), selectedUnit = unit.getSelectedItem().toString();
            if (existing != null) {
                databaseExecutor.execute(() -> {
                    Product product = database.productDao().getById(existing.id);
                    if (product != null) { product.name=productName; product.category=selectedCategory; product.unit=selectedUnit; product.minimumStock=min; database.productDao().update(product); }
                    runOnUiThread(() -> { dialog.dismiss(); loadInventory(); toast("Product updated"); });
                });
            } else {
                Double qty = parsePositive(quantity, "Purchased quantity"); if (qty == null) return;
                Double cost = parseOptionalNonNegative(price, "Purchase price"); if (cost == INVALID) return;
                Product product = new Product(); product.name=productName; product.category=selectedCategory; product.unit=selectedUnit; product.minimumStock=min;
                GroceryBatch batch = makeBatch(qty, cost, dates[0], dates[1]);
                databaseExecutor.execute(() -> {
                    RoomTransaction.run(database, () -> { long id=database.productDao().insert(product); product.id=id; batch.productId=id; long batchId=database.batchDao().insert(batch); database.stockHistoryDao().insert(purchaseHistory(id,batchId,product.name,product.unit,batch.purchasedQuantity)); });
                    runOnUiThread(() -> { dialog.dismiss(); loadInventory(); toast("Product and purchase batch saved"); });
                });
            }
        }));
        dialog.show();
    }

    private void loadProductForEdit(long id) {
        databaseExecutor.execute(() -> { Product p=database.productDao().getById(id); runOnUiThread(() -> { if (p != null && !p.archived) showProductDialog(p); }); });
    }

    private void showBatchDialog(long productId, String productName, String productUnit) {
        View form=LayoutInflater.from(this).inflate(R.layout.dialog_batch, null);
        EditText quantity=form.findViewById(R.id.batchQuantityInput), price=form.findViewById(R.id.batchPriceInput);
        Button purchase=form.findViewById(R.id.batchPurchaseDateButton), expiry=form.findViewById(R.id.batchExpiryDateButton);
        LocalDate[] dates={LocalDate.now(), LocalDate.now().plusDays(7)};
        setDateButton(purchase, dates, 0, "Purchase date"); setDateButton(expiry, dates, 1, "Expiry date");
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Record purchase: " + productName + " (" + productUnit + ")").setView(form).setNegativeButton("Cancel", null).setPositiveButton("Save batch", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Double qty=parsePositive(quantity,"Purchased quantity"); if(qty==null)return;
            Double cost=parseOptionalNonNegative(price,"Purchase price"); if(cost==INVALID)return;
            GroceryBatch batch=makeBatch(qty,cost,dates[0],dates[1]); batch.productId=productId;
            databaseExecutor.execute(() -> { RoomTransaction.run(database, () -> { long batchId=database.batchDao().insert(batch); database.stockHistoryDao().insert(purchaseHistory(productId,batchId,productName,productUnit,batch.purchasedQuantity)); }); runOnUiThread(() -> { dialog.dismiss(); loadInventory(); toast("Purchase batch recorded"); }); });
        }));
        dialog.show();
    }

    private void showTransactionDialog(InventoryItem selected, boolean waste) {
        View form=LayoutInflater.from(this).inflate(R.layout.dialog_stock_transaction,null);
        EditText quantity=form.findViewById(R.id.transactionQuantityInput);
        Spinner reason=form.findViewById(R.id.wasteReasonInput);
        String[] reasons={"Select a reason", "Expired", "Spoiled", "Excess purchase", "Other"};
        reason.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,reasons));
        reason.setVisibility(waste?View.VISIBLE:View.GONE);
        String action=waste?"WASTED":"CONSUMED";
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(waste?"Record food waste":"Record consumption")
                .setMessage(selected.name+" • "+selected.remainingQuantity+" "+selected.unit+" available")
                .setView(form).setNegativeButton("Cancel",null).setPositiveButton(waste?"Record waste":"Record consumption",null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Double amount=parsePositive(quantity,"Quantity"); if(amount==null)return;
            String selectedReason=null;
            if(waste) { selectedReason=reason.getSelectedItem().toString(); if(reasons[0].equals(selectedReason)) { Toast.makeText(this,"Choose a waste reason",Toast.LENGTH_SHORT).show(); return; } }
            final String reasonValue=selectedReason;
            databaseExecutor.execute(() -> {
                String error=null;
                try {
                    RoomTransaction.run(database, () -> {
                        int changed=database.batchDao().subtractStock(selected.batchId,amount);
                        if(changed!=1) throw new IllegalArgumentException("Quantity exceeds the batch's remaining stock. Refresh and try again.");
                        Product product=database.productDao().getById(selected.productId);
                        if(product==null) throw new IllegalStateException("Product record is unavailable.");
                        StockHistory history=new StockHistory(); history.productId=product.id; history.batchId=selected.batchId; history.productName=product.name; history.unit=product.unit;
                        history.actionType=action; history.quantity=amount; history.reason=reasonValue; history.timestamp=System.currentTimeMillis(); database.stockHistoryDao().insert(history);
                    });
                } catch(Exception ex) { error=ex.getMessage()==null?"Transaction could not be saved.":ex.getMessage(); }
                String message=error;
                runOnUiThread(() -> { if(message==null) { dialog.dismiss(); loadInventory(); toast(waste?"Waste recorded":"Consumption recorded"); } else toast(message); });
            });
        })); dialog.show();
    }

    private StockHistory purchaseHistory(long productId,long batchId,String name,String unit,double quantity) {
        StockHistory history=new StockHistory(); history.productId=productId; history.batchId=batchId; history.productName=name; history.unit=unit;
        history.actionType="PURCHASE"; history.quantity=quantity; history.timestamp=System.currentTimeMillis(); return history;
    }

    private GroceryBatch makeBatch(double quantity, Double price, LocalDate purchaseDate, LocalDate expiryDate) {
        GroceryBatch batch=new GroceryBatch(); batch.purchasedQuantity=quantity; batch.remainingQuantity=quantity;
        batch.purchaseDate=purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE); batch.expiryDate=expiryDate.format(DateTimeFormatter.ISO_LOCAL_DATE); batch.purchasePrice=price;
        return batch;
    }

    private void setDateButton(Button button, LocalDate[] dates, int index, String label) {
        refreshDateLabel(button, dates[index], label);
        button.setOnClickListener(v -> {
            LocalDate date=dates[index];
            new DatePickerDialog(this, (picker, year, month, day) -> { dates[index]=LocalDate.of(year,month+1,day); refreshDateLabel(button, dates[index], label); }, date.getYear(), date.getMonthValue()-1, date.getDayOfMonth()).show();
        });
    }
    private void refreshDateLabel(Button button, LocalDate date, String label) { button.setText(label + ": " + date.format(DateTimeFormatter.ISO_LOCAL_DATE)); }

    private Double parsePositive(EditText field, String label) {
        String raw=field.getText().toString().trim();
        try { double value=Double.parseDouble(raw); if (!Double.isFinite(value) || value<=0) throw new NumberFormatException(); return value; }
        catch (Exception ex) { field.setError(label + " must be greater than zero"); return null; }
    }
    private Double parseNonNegative(EditText field, String label) {
        String raw=field.getText().toString().trim();
        try { double value=Double.parseDouble(raw); if (!Double.isFinite(value) || value<0) throw new NumberFormatException(); return value; }
        catch (Exception ex) { field.setError(label + " must be zero or more"); return null; }
    }
    private static final Double INVALID=Double.NEGATIVE_INFINITY;
    private Double parseOptionalNonNegative(EditText field, String label) {
        String raw=field.getText().toString().trim(); if(raw.isEmpty())return null;
        Double value=parseNonNegative(field,label); return value==null?INVALID:value;
    }
    private int indexOf(String[] values, String selected) { for(int i=0;i<values.length;i++)if(values[i].equals(selected))return i; return values.length-1; }
    private String number(double value) { return value == Math.rint(value) ? String.format(Locale.ROOT,"%.0f",value) : Double.toString(value); }

    private void confirmDelete(InventoryItem item) {
        new AlertDialog.Builder(this).setTitle("Delete " + item.name + "?")
                .setMessage("If this product has purchase batches, it will be archived and hidden so its records remain preserved. Otherwise it will be deleted.")
                .setNegativeButton("Cancel", null).setPositiveButton("Delete", (d,w) -> databaseExecutor.execute(() -> {
                    List<GroceryBatch> batches=database.batchDao().getForProduct(item.productId);
                    if(batches.isEmpty()) database.productDao().delete(item.productId); else database.productDao().archive(item.productId);
                    runOnUiThread(() -> { loadInventory(); toast(batches.isEmpty()?"Product deleted":"Product archived; purchase records preserved"); });
                })).show();
    }
    private void toast(String message) { Toast.makeText(this,message,Toast.LENGTH_SHORT).show(); }
    @Override protected void onDestroy() { super.onDestroy(); databaseExecutor.shutdown(); }
}
