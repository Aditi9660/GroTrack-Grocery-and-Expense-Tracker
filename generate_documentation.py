import os
import docx
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def create_element(name):
    return OxmlElement(name)

def set_run_font(run, bold=False, italic=False):
    run.font.name = 'Times New Roman'
    run.font.size = Pt(12)
    run.font.color.rgb = RGBColor(0, 0, 0)
    run.bold = bold
    run.italic = italic
    rPr = run._r.get_or_add_rPr()
    rFonts = parse_xml(r'<w:rFonts %s w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman" w:eastAsia="Times New Roman"/>' % nsdecls('w'))
    rPr.append(rFonts)

def add_p(container, text="", bold=False, italic=False, align=WD_ALIGN_PARAGRAPH.LEFT, space_before=0, space_after=4, line_spacing=1.15):
    p = container.add_paragraph()
    p.alignment = align
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = line_spacing
    if text:
        run = p.add_run(text)
        set_run_font(run, bold=bold, italic=italic)
    return p

def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(r'<w:tcMar %s>'
                      r'<w:top w:w="%d" w:type="dxa"/>'
                      r'<w:bottom w:w="%d" w:type="dxa"/>'
                      r'<w:left w:w="%d" w:type="dxa"/>'
                      r'<w:right w:w="%d" w:type="dxa"/>'
                      r'</w:tcMar>' % (nsdecls('w'), top, bottom, left, right))
    tcPr.append(tcMar)

def set_cell_shading(cell, color_hex="F2F2F2"):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(r'<w:shd %s w:val="clear" w:color="auto" w:fill="%s"/>' % (nsdecls('w'), color_hex))
    tcPr.append(shd)

def set_table_borders(table, color="A0A0A0", sz="4", val="single"):
    tblPr = table._tbl.tblPr
    borders = parse_xml(r'<w:tblBorders %s>'
                        r'<w:top w:val="%s" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:bottom w:val="%s" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:left w:val="%s" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:right w:val="%s" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:insideH w:val="%s" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:insideV w:val="%s" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'</w:tblBorders>' % (nsdecls('w'), val, sz, color, val, sz, color, val, sz, color, val, sz, color, val, sz, color, val, sz, color))
    tblPr.append(borders)

def set_placeholder_box_borders(table, color="888888", sz="6"):
    tblPr = table._tbl.tblPr
    borders = parse_xml(r'<w:tblBorders %s>'
                        r'<w:top w:val="dashed" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:bottom w:val="dashed" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:left w:val="dashed" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:right w:val="dashed" w:sz="%s" w:space="0" w:color="%s"/>'
                        r'<w:insideH w:val="none"/>'
                        r'<w:insideV w:val="none"/>'
                        r'</w:tblBorders>' % (nsdecls('w'), sz, color, sz, color, sz, color, sz, color))
    tblPr.append(borders)

def build_grotrack_documentation(output_path):
    doc = Document()
    
    # Configure 0.75-inch page margins (Usable width = 7.0 inches)
    for section in doc.sections:
        section.top_margin = Inches(0.75)
        section.bottom_margin = Inches(0.75)
        section.left_margin = Inches(0.75)
        section.right_margin = Inches(0.75)
        section.page_width = Inches(8.5)
        section.page_height = Inches(11.0)

    # Set default style
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Times New Roman'
    normal_style.font.size = Pt(12)
    normal_style.font.color.rgb = RGBColor(0, 0, 0)
    normal_rPr = normal_style._element.get_or_add_rPr()
    normal_rFonts = parse_xml(r'<w:rFonts %s w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman" w:eastAsia="Times New Roman"/>' % nsdecls('w'))
    normal_rPr.append(normal_rFonts)

    # ==========================================
    # PAGE 1: INTRODUCTION ONLY
    # ==========================================
    add_p(doc, "GroTrack — Household Grocery Inventory & Food Waste Tracker", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, space_before=6, space_after=2)
    add_p(doc, "System Architecture & Implementation Technical Documentation", italic=True, align=WD_ALIGN_PARAGRAPH.CENTER, space_before=0, space_after=14)

    add_p(doc, "1. Project Overview & Motivation", bold=True, space_before=6, space_after=2)
    add_p(doc, "GroTrack is an offline-first Android application developed to address the widespread challenge of household grocery mismanagement, accidental food expiration, and unnecessary domestic food waste. By operating as a smart pantry and loss-tracking ledger, the application provides domestic households with continuous visibility into available food stocks, imminent expiration milestones, minimum inventory thresholds, and recorded food consumption patterns.", space_after=8)

    add_p(doc, "2. Key Design Principles & Solutions", bold=True, space_before=6, space_after=2)
    add_p(doc, "• Decoupled Multi-Batch Architecture: Unlike standard grocery lists that overwrite stock amounts into a single number, GroTrack records every purchase as an independent grocery batch linked to a parent product. This ensures multiple purchases of the same item (such as milk or bread) maintain distinct purchase dates, expiry dates, and unit prices.\n"
               "• Expiry Notification Daemon: An automated background worker built with AndroidX WorkManager inspects active pantry batches daily and posts system notifications for items expiring within three days or already expired.\n"
               "• Integrated Shortfall Shopping List: The shopping system analyzes live stock across all batches against user-defined minimum stock thresholds, generating one-tap suggestions for items needing replenishment.\n"
               "• ACID Stock Ledger Transactions: Consuming or wasting groceries executes an atomic SQLite transaction via Room, decrementing the batch remaining quantity while simultaneously appending an immutable audit row into the stock history.", space_after=8)

    add_p(doc, "3. System Architecture & Technology Stack", bold=True, space_before=6, space_after=2)
    add_p(doc, "• Programming Language & Runtime: Java 11 application logic with Android SDK 28 (Android 9 Pie) to SDK 36.\n"
               "• User Interface: Native Android XML layouts utilizing Google Material Components 3 (MaterialCardView, ChipGroup, Shapeable Buttons).\n"
               "• Local Persistence: AndroidX Room ORM 2.7.2 backed by SQLite with transactional guarantees.\n"
               "• Background Services: WorkManager 2.12.0 for periodic system-scheduled expiry verification.", space_after=8)

    add_p(doc, "4. Relational Database Schema Overview", bold=True, space_before=6, space_after=2)
    add_p(doc, "The SQLite database structure comprises four interrelated relational entities:\n"
               "1. products: Master definition of grocery items containing product name, category, measurement unit, minimum threshold, and archive status.\n"
               "2. grocery_batches: Individual purchase records storing purchased quantity, remaining quantity, purchase date, expiration date, unit price, and product foreign key.\n"
               "3. shopping_list_items: Planned grocery items with requested quantity, measurement unit, optional product link, and purchased flag.\n"
               "4. stock_history: Permanent transaction audit trail recording event type (PURCHASE, CONSUMED, WASTED), quantity, snapshot unit, waste reason, batch foreign key, and epoch timestamp.", space_after=14)

    # STRICT REQUIREMENT: First page is ONLY the introduction. Break to next page.
    doc.add_page_break()

    # ==========================================
    # PAGE 2 ONWARDS: FEATURES & SCREENS
    # 2 Column format (Col 1: XML, Col 2: Java)
    # Below: Screenshot Space
    # ==========================================

    features = [
        {
            "num": 1,
            "title": "Home Dashboard Screen & Expiry Metrics",
            "desc": "The central launch dashboard displays macro-level stock counts (total active products, batches expiring soon, low-stock alerts), quick-access navigation cards to all sub-modules, a 7-day domestic waste summary, and real-time alert trigger controls.",
            "xml": 
"""<!-- activity_dashboard.xml excerpt -->
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/grotrack_background"
    android:fillViewport="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <!-- App Header Branding -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center_vertical"
            android:orientation="horizontal">
            <ImageView
                android:layout_width="52dp"
                android:layout_height="52dp"
                android:src="@drawable/ic_grotrack_logo" />
            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:paddingStart="12dp">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="GroTrack"
                    android:textColor="@color/grotrack_dark_green"
                    android:textSize="25sp"
                    android:textStyle="bold" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Track Smart. Waste Less."
                    android:textColor="@color/grotrack_secondary"
                    android:textSize="13sp" />
            </LinearLayout>
        </LinearLayout>

        <!-- Metric Summary Cards -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal">
            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="92dp"
                android:layout_weight="1"
                app:cardBackgroundColor="@color/grotrack_light_green"
                app:cardCornerRadius="18dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:gravity="center"
                    android:orientation="vertical">
                    <TextView
                        android:id="@+id/homeProductCount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textColor="@color/grotrack_dark_green"
                        android:textSize="25sp"
                        android:textStyle="bold" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Products" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="92dp"
                android:layout_weight="1"
                app:cardBackgroundColor="#FFF4D8"
                app:cardCornerRadius="18dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:gravity="center"
                    android:orientation="vertical">
                    <TextView
                        android:id="@+id/homeExpiringCount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textColor="#8A5A00"
                        android:textSize="25sp"
                        android:textStyle="bold" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Expiring soon" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="92dp"
                android:layout_weight="1"
                app:cardBackgroundColor="#FCE8E6"
                app:cardCornerRadius="18dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:gravity="center"
                    android:orientation="vertical">
                    <TextView
                        android:id="@+id/homeLowStockCount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textColor="@color/grotrack_expired"
                        android:textSize="25sp"
                        android:textStyle="bold" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Low stock" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

        <!-- Quick Access Navigation Cards -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/navInventoryCard"
                android:layout_width="0dp"
                android:layout_height="84dp"
                android:layout_weight="1">
                <TextView android:text="My Inventory" />
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/navShoppingCard"
                android:layout_width="0dp"
                android:layout_height="84dp"
                android:layout_weight="1">
                <TextView android:text="Shopping List" />
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>
    </LinearLayout>
</ScrollView>""",
            "java":
"""// DashboardActivity.java excerpt
public class DashboardActivity extends AppCompatActivity {
    private final ExecutorService executor = 
        Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private TextView productsCount, expiringCount;
    private TextView lowStockCount, inventoryDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(
            getWindow(), true);
        setContentView(R.layout.activity_dashboard);
        database = GroTrackDatabase.getInstance(
            getApplicationContext());
        ExpiryWorkScheduler.schedulePeriodic(
            getApplicationContext());

        productsCount = findViewById(R.id.homeProductCount);
        expiringCount = findViewById(R.id.homeExpiringCount);
        lowStockCount = findViewById(R.id.homeLowStockCount);
        inventoryDetails = findViewById(R.id.homeInventoryDetails);

        findViewById(R.id.navInventoryCard)
            .setOnClickListener(v -> startActivity(
                new Intent(this, MainActivity.class)));
        findViewById(R.id.navShoppingCard)
            .setOnClickListener(v -> startActivity(
                new Intent(this, ShoppingListActivity.class)));
        findViewById(R.id.navWasteCard)
            .setOnClickListener(v -> startActivity(
                new Intent(this, WasteHistoryActivity.class)));
        findViewById(R.id.navInsightsCard)
            .setOnClickListener(v -> startActivity(
                new Intent(this, InsightsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void loadDashboard() {
        executor.execute(() -> {
            LocalDate today = LocalDate.now();
            List<Product> products = 
                database.productDao().getAll();
            List<InventoryItem> batches = 
                database.batchDao().getInventory();
            long weekStart = today.minusDays(6)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
            long tomorrow = today.plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
            List<HistoryItem> waste = database
                .stockHistoryDao()
                .getWasteHistory(weekStart, tomorrow, null);

            DashboardSummary summary = 
                calculate(products, batches, waste, today);
            runOnUiThread(() -> render(summary));
        });
    }

    private void render(DashboardSummary summary) {
        productsCount.setText(
            String.valueOf(summary.products));
        expiringCount.setText(
            String.valueOf(summary.expiring));
        lowStockCount.setText(
            String.valueOf(summary.low));
        inventoryDetails.setText(
            "Expired batches with stock: " + summary.expired
            + "\\nProducts below minimum: " + summary.low);
    }
}"""
        },
        {
            "num": 2,
            "title": "My Inventory Screen with Multi-Parameter Filters & Search",
            "desc": "The inventory screen displays all active household food batches. It features live text searching, category Chip filtering (Dairy, Vegetables, Fruits, Grains, etc.), and expiration status filtering (Fresh, Expiring Soon, Expired).",
            "xml":
"""<!-- activity_main.xml excerpt -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/grotrack_background"
    android:padding="16dp">

    <!-- Screen Title and Navigation -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:orientation="horizontal">
        <Button
            android:id="@+id/inventoryHomeButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="‹ Home" />
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="My Inventory"
            android:textColor="@color/grotrack_dark_green"
            android:textSize="24sp"
            android:textStyle="bold" />
    </LinearLayout>

    <!-- Real-time Text Filter -->
    <EditText
        android:id="@+id/searchInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Search product name"
        android:singleLine="true" />

    <!-- Material Category Chip Group -->
    <HorizontalScrollView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scrollbars="none">
        <com.google.android.material.chip.ChipGroup
            android:id="@+id/categoryFilterGroup"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:singleSelection="true"
            app:selectionRequired="true" />
    </HorizontalScrollView>

    <!-- Expiry Status Filter Spinner -->
    <Spinner
        android:id="@+id/statusFilter"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <!-- Batches Recycler View -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/inventoryList"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
</LinearLayout>""",
            "java":
"""// MainActivity.java excerpt
public class MainActivity extends AppCompatActivity {
    private static final String[] CATEGORIES = {
        "All categories", "Dairy", "Vegetables",
        "Fruits", "Grains", "Packaged Food", "Other"};
    private static final String[] STATUSES = {
        "All statuses", "Fresh", "Expiring Soon", "Expired"};
    private final ExecutorService databaseExecutor = 
        Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private InventoryAdapter adapter;
    private final List<InventoryItem> inventory = new ArrayList<>();
    private EditText searchInput;
    private ChipGroup categoryFilterGroup;
    private Spinner statusFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = GroTrackDatabase.getInstance(this);

        RecyclerView list = findViewById(R.id.inventoryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(new InventoryAdapter.Actions() {
            @Override public void addBatch(InventoryItem item) {
                showBatchDialog(item.productId, item.name, item.unit);
            }
            @Override public void edit(InventoryItem item) {
                loadProductForEdit(item.productId);
            }
            @Override public void delete(InventoryItem item) {
                confirmDelete(item);
            }
            @Override public void consume(InventoryItem item) {
                showTransactionDialog(item, false);
            }
            @Override public void waste(InventoryItem item) {
                showTransactionDialog(item, true);
            }
        });
        list.setAdapter(adapter);

        searchInput = findViewById(R.id.searchInput);
        categoryFilterGroup = findViewById(R.id.categoryFilterGroup);
        statusFilter = findViewById(R.id.statusFilter);
        setupCategoryFilters();
        setupSpinner(statusFilter, STATUSES);

        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                applyFilters();
            }
            public void afterTextChanged(Editable s) {}
        });
        categoryFilterGroup.setOnCheckedStateChangeListener(
            (g, ids) -> applyFilters());
        statusFilter.setOnItemSelectedListener(
            new SimpleSelectionListener(this::applyFilters));
    }

    private void applyFilters() {
        String query = searchInput.getText()
            .toString().trim().toLowerCase(Locale.ROOT);
        View checked = categoryFilterGroup.findViewById(
            categoryFilterGroup.getCheckedChipId());
        String cat = checked instanceof Chip ? 
            ((Chip) checked).getText().toString() : CATEGORIES[0];
        String filterStat = statusFilter.getSelectedItem().toString();

        List<InventoryItem> filtered = new ArrayList<>();
        for (InventoryItem item : inventory) {
            if (item.archived || !item.name.toLowerCase(Locale.ROOT).contains(query)) continue;
            if (!CATEGORIES[0].equals(cat) && !cat.equals(item.category)) continue;
            if (!STATUSES[0].equals(filterStat) && !filterStat.equals(status(item.expiryDate))) continue;
            filtered.add(item);
        }
        adapter.submit(filtered);
    }
}"""
        },
        {
            "num": 3,
            "title": "Add / Edit Grocery Product & Initial Batch Dialog",
            "desc": "Allows creation of master product entries with categorical classification, physical unit specification (kg, g, litre, ml, piece), minimum desired stock, and records the initial purchased batch within a single atomic operation.",
            "xml":
"""<!-- dialog_product.xml -->
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:fillViewport="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <EditText
            android:id="@+id/productNameInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Product name"
            android:inputType="textCapSentences" />

        <Spinner
            android:id="@+id/productCategoryInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

        <Spinner
            android:id="@+id/productUnitInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

        <EditText
            android:id="@+id/minimumStockInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Minimum stock level (0 or more)"
            android:inputType="numberDecimal" />

        <TextView
            android:id="@+id/batchHeading"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Initial purchase batch"
            android:textStyle="bold"
            android:textSize="18sp"
            android:paddingTop="12dp" />

        <EditText
            android:id="@+id/quantityInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Purchased quantity (>0)"
            android:inputType="numberDecimal" />

        <EditText
            android:id="@+id/priceInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Purchase price (optional)"
            android:inputType="numberDecimal" />

        <Button
            android:id="@+id/purchaseDateButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Purchase date" />

        <Button
            android:id="@+id/expiryDateButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Expiry date" />
    </LinearLayout>
</ScrollView>""",
            "java":
"""// MainActivity.java - showProductDialog
private void showProductDialog(Product existing) {
    View form = LayoutInflater.from(this)
        .inflate(R.layout.dialog_product, null);
    EditText name = form.findViewById(R.id.productNameInput);
    EditText minimum = form.findViewById(R.id.minimumStockInput);
    EditText quantity = form.findViewById(R.id.quantityInput);
    EditText price = form.findViewById(R.id.priceInput);
    Spinner category = form.findViewById(R.id.productCategoryInput);
    Spinner unit = form.findViewById(R.id.productUnitInput);
    setupSpinner(category, PRODUCT_CATEGORIES);
    setupSpinner(unit, UNITS);

    Button purchaseDate = form.findViewById(R.id.purchaseDateButton);
    Button expiryDate = form.findViewById(R.id.expiryDateButton);
    LocalDate[] dates = {LocalDate.now(), LocalDate.now().plusDays(7)};
    setDateButton(purchaseDate, dates, 0, "Purchase date");
    setDateButton(expiryDate, dates, 1, "Expiry date");

    if (existing != null) {
        name.setText(existing.name);
        minimum.setText(number(existing.minimumStock));
        category.setSelection(indexOf(PRODUCT_CATEGORIES, existing.category));
        unit.setSelection(indexOf(UNITS, existing.unit));
        unit.setEnabled(false); // Lock unit to preserve batch consistency
        form.findViewById(R.id.batchHeading).setVisibility(View.GONE);
        quantity.setVisibility(View.GONE);
        price.setVisibility(View.GONE);
        purchaseDate.setVisibility(View.GONE);
        expiryDate.setVisibility(View.GONE);
    }

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(existing == null ? "Add grocery product" : "Edit product")
        .setView(form)
        .setNegativeButton("Cancel", null)
        .setPositiveButton(existing == null ? "Save product" : "Save changes", null)
        .create();

    dialog.setOnShowListener(d -> dialog.getButton(
        AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        String pName = name.getText().toString().trim();
        Double min = parseNonNegative(minimum, "Minimum stock");
        if (pName.isEmpty()) { name.setError("Required"); return; }
        if (min == null) return;
        String cat = category.getSelectedItem().toString();
        String un = unit.getSelectedItem().toString();

        if (existing == null) {
            Double qty = parsePositive(quantity, "Quantity");
            if (qty == null) return;
            Double cost = parseOptionalNonNegative(price, "Price");
            if (cost == INVALID) return;
            Product product = new Product();
            product.name = pName; product.category = cat;
            product.unit = un; product.minimumStock = min;
            GroceryBatch batch = makeBatch(qty, cost, dates[0], dates[1]);

            databaseExecutor.execute(() -> {
                RoomTransaction.run(database, () -> {
                    long id = database.productDao().insert(product);
                    product.id = id; batch.productId = id;
                    long bId = database.batchDao().insert(batch);
                    database.stockHistoryDao().insert(
                        purchaseHistory(id, bId, product.name, product.unit, batch.purchasedQuantity));
                });
                runOnUiThread(() -> {
                    dialog.dismiss(); loadInventory(); toast("Product and batch saved");
                });
            });
        }
    }));
    dialog.show();
}"""
        },
        {
            "num": 4,
            "title": "Add New Purchase Batch Dialog (Repeat Purchases)",
            "desc": "Enables recording additional batches for an already registered grocery item. Each new batch retains its own purchase timestamp, quantity, unique expiration date, and optional cost.",
            "xml":
"""<!-- dialog_batch.xml -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">

    <EditText
        android:id="@+id/batchQuantityInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Purchased quantity (greater than 0)"
        android:inputType="numberDecimal" />

    <EditText
        android:id="@+id/batchPriceInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Purchase price (optional)"
        android:inputType="numberDecimal" />

    <Button
        android:id="@+id/batchPurchaseDateButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Purchase date"
        android:textAllCaps="false" />

    <Button
        android:id="@+id/batchExpiryDateButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Expiry date"
        android:textAllCaps="false" />
</LinearLayout>""",
            "java":
"""// MainActivity.java - showBatchDialog
private void showBatchDialog(long productId, String productName, String productUnit) {
    View form = LayoutInflater.from(this)
        .inflate(R.layout.dialog_batch, null);
    EditText quantity = form.findViewById(R.id.batchQuantityInput);
    EditText price = form.findViewById(R.id.batchPriceInput);
    Button purchase = form.findViewById(R.id.batchPurchaseDateButton);
    Button expiry = form.findViewById(R.id.batchExpiryDateButton);
    LocalDate[] dates = {LocalDate.now(), LocalDate.now().plusDays(7)};

    setDateButton(purchase, dates, 0, "Purchase date");
    setDateButton(expiry, dates, 1, "Expiry date");

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("Record purchase: " + productName + " (" + productUnit + ")")
        .setView(form)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save batch", null)
        .create();

    dialog.setOnShowListener(d -> dialog.getButton(
        AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        Double qty = parsePositive(quantity, "Purchased quantity");
        if (qty == null) return;
        Double cost = parseOptionalNonNegative(price, "Purchase price");
        if (cost == INVALID) return;

        GroceryBatch batch = makeBatch(qty, cost, dates[0], dates[1]);
        batch.productId = productId;

        databaseExecutor.execute(() -> {
            RoomTransaction.run(database, () -> {
                long batchId = database.batchDao().insert(batch);
                database.stockHistoryDao().insert(
                    purchaseHistory(productId, batchId, productName, productUnit, batch.purchasedQuantity));
            });
            runOnUiThread(() -> {
                dialog.dismiss();
                loadInventory();
                toast("Purchase batch recorded");
            });
        });
    }));
    dialog.show();
}"""
        },
        {
            "num": 5,
            "title": "Stock Consumption & Food Waste Action Dialog",
            "desc": "Provides verified stock deduction modals for either normal consumption or waste disposal. Enforces that deduction does not exceed remaining quantity and persists the event into the stock history.",
            "xml":
"""<!-- dialog_stock_transaction.xml -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">

    <EditText
        android:id="@+id/transactionQuantityInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Quantity (greater than 0)"
        android:inputType="numberDecimal" />

    <!-- Reason dropdown displayed only when action is Waste -->
    <Spinner
        android:id="@+id/wasteReasonInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>""",
            "java":
"""// MainActivity.java - showTransactionDialog
private void showTransactionDialog(InventoryItem selected, boolean waste) {
    View form = LayoutInflater.from(this)
        .inflate(R.layout.dialog_stock_transaction, null);
    EditText quantity = form.findViewById(R.id.transactionQuantityInput);
    Spinner reason = form.findViewById(R.id.wasteReasonInput);
    String[] reasons = {"Select a reason", "Expired", "Spoiled", "Excess purchase", "Other"};
    reason.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reasons));
    reason.setVisibility(waste ? View.VISIBLE : View.GONE);
    String action = waste ? "WASTED" : "CONSUMED";

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(waste ? "Record food waste" : "Record consumption")
        .setMessage(selected.name + " • " + selected.remainingQuantity + " " + selected.unit + " available")
        .setView(form)
        .setNegativeButton("Cancel", null)
        .setPositiveButton(waste ? "Record waste" : "Record consumption", null)
        .create();

    dialog.setOnShowListener(d -> dialog.getButton(
        AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        Double amount = parsePositive(quantity, "Quantity");
        if (amount == null) return;
        String selectedReason = null;
        if (waste) {
            selectedReason = reason.getSelectedItem().toString();
            if (reasons[0].equals(selectedReason)) {
                Toast.makeText(this, "Choose a waste reason", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final String reasonValue = selectedReason;
        databaseExecutor.execute(() -> {
            String error = null;
            try {
                RoomTransaction.run(database, () -> {
                    int changed = database.batchDao().subtractStock(selected.batchId, amount);
                    if (changed != 1) {
                        throw new IllegalArgumentException("Quantity exceeds available stock.");
                    }
                    Product product = database.productDao().getById(selected.productId);
                    StockHistory history = new StockHistory();
                    history.productId = product.id;
                    history.batchId = selected.batchId;
                    history.productName = product.name;
                    history.unit = product.unit;
                    history.actionType = action;
                    history.quantity = amount;
                    history.reason = reasonValue;
                    history.timestamp = System.currentTimeMillis();
                    database.stockHistoryDao().insert(history);
                });
            } catch (Exception ex) {
                error = ex.getMessage();
            }
            String message = error;
            runOnUiThread(() -> {
                if (message == null) {
                    dialog.dismiss();
                    loadInventory();
                    toast(waste ? "Waste recorded" : "Consumption recorded");
                } else toast(message);
            });
        });
    }));
    dialog.show();
}"""
        },
        {
            "num": 6,
            "title": "Smart Shopping List Screen & Purchase Conversion",
            "desc": "Manages domestic purchase planning. Planned items only enter active household inventory once an actual purchase confirmation is executed, converting the shopping list row into a verified batch.",
            "xml":
"""<!-- activity_shopping_list.xml -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@color/grotrack_background">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Shopping List"
        android:textSize="26sp"
        android:textStyle="bold"
        android:textColor="@color/grotrack_dark_green" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Items enter inventory only after recording purchase."
        android:textColor="@color/grotrack_secondary"
        android:paddingBottom="8dp" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        <Button
            android:id="@+id/suggestLowStockButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Suggest low stock" />
        <Button
            android:id="@+id/addShoppingItemButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Add item" />
    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/shoppingList"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
</LinearLayout>""",
            "java":
"""// ShoppingListActivity.java - showPurchaseDialog
private void showPurchaseDialog(ShoppingListItem listed) {
    View form = LayoutInflater.from(this)
        .inflate(R.layout.dialog_batch, null);
    EditText quantity = form.findViewById(R.id.batchQuantityInput);
    EditText price = form.findViewById(R.id.batchPriceInput);
    Button purchaseDate = form.findViewById(R.id.batchPurchaseDateButton);
    Button expiryDate = form.findViewById(R.id.batchExpiryDateButton);
    quantity.setText(number(listed.quantity));
    LocalDate[] dates = {LocalDate.now(), LocalDate.now().plusDays(7)};
    setDateButton(purchaseDate, dates, 0, "Purchase date");
    setDateButton(expiryDate, dates, 1, "Expiry date");

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("Record purchase: " + listed.itemName)
        .setMessage("Inventory stock changes only when purchase is saved.")
        .setView(form)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save purchase", null)
        .create();

    dialog.setOnShowListener(d -> dialog.getButton(
        AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        Double amount = parsePositive(quantity, "Purchased quantity");
        if (amount == null) return;
        Double cost = parseOptionalPrice(price);
        if (cost == INVALID) return;

        executor.execute(() -> {
            RoomTransaction.run(database, () -> {
                ShoppingListItem fresh = database.shoppingListDao().getById(listed.id);
                Product product = fresh.productId == null ?
                    database.productDao().findActiveByNameAndUnit(fresh.itemName, fresh.unit) :
                    database.productDao().getById(fresh.productId);
                if (product == null || product.archived) {
                    product = new Product();
                    product.name = fresh.itemName; product.category = "Other";
                    product.unit = fresh.unit; product.minimumStock = 0;
                    product.id = database.productDao().insert(product);
                }
                GroceryBatch batch = new GroceryBatch();
                batch.productId = product.id;
                batch.purchasedQuantity = amount;
                batch.remainingQuantity = amount;
                batch.purchaseDate = dates[0].format(DateTimeFormatter.ISO_LOCAL_DATE);
                batch.expiryDate = dates[1].format(DateTimeFormatter.ISO_LOCAL_DATE);
                batch.purchasePrice = cost;
                long batchId = database.batchDao().insert(batch);

                StockHistory history = new StockHistory();
                history.productId = product.id; history.batchId = batchId;
                history.productName = product.name; history.unit = product.unit;
                history.actionType = "PURCHASE"; history.quantity = amount;
                history.timestamp = System.currentTimeMillis();
                database.stockHistoryDao().insert(history);

                fresh.purchased = true;
                database.shoppingListDao().update(fresh);
            });
            runOnUiThread(() -> {
                dialog.dismiss(); loadItems(); toast("Batch created in inventory");
            });
        });
    }));
    dialog.show();
}"""
        },
        {
            "num": 7,
            "title": "Low-Stock Suggestions & Manual Shopping Item Dialog",
            "desc": "Calculates stock deficits by comparing current inventory totals against product minimum stock targets. Allows instant automated batch population or manual single-item entry.",
            "xml":
"""<!-- dialog_shopping_item.xml -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">

    <EditText
        android:id="@+id/shoppingNameInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Item name"
        android:inputType="textCapSentences" />

    <EditText
        android:id="@+id/shoppingQuantityInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Quantity (greater than 0)"
        android:inputType="numberDecimal" />

    <Spinner
        android:id="@+id/shoppingUnitInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>""",
            "java":
"""// ShoppingListActivity.java - addLowStockSuggestions
private void addLowStockSuggestions() {
    executor.execute(() -> {
        List<Product> products = database.productDao().getAll();
        List<InventoryItem> rows = database.batchDao().getInventory();
        List<ShoppingListItem> list = database.shoppingListDao().getAll();
        Map<Long, Double> totals = new HashMap<>();

        for (InventoryItem row : rows) {
            if (!row.archived) {
                totals.put(row.productId,
                    totals.getOrDefault(row.productId, 0d) + row.remainingQuantity);
            }
        }
        int added = 0;
        for (Product product : products) {
            if (product.archived) continue;
            double deficit = product.minimumStock - totals.getOrDefault(product.id, 0d);
            if (deficit <= 0) continue;

            boolean already = false;
            for (ShoppingListItem listed : list) {
                if (!listed.purchased && listed.productId != null && listed.productId == product.id) {
                    already = true; break;
                }
            }
            if (!already) {
                ShoppingListItem suggestion = new ShoppingListItem();
                suggestion.productId = product.id;
                suggestion.itemName = product.name;
                suggestion.quantity = deficit;
                suggestion.unit = product.unit;
                suggestion.createdAt = System.currentTimeMillis();
                database.shoppingListDao().insert(suggestion);
                added++;
            }
        }
        int count = added;
        runOnUiThread(() -> {
            loadItems();
            toast(count == 0 ? "No new low-stock suggestions" :
                "Added " + count + " suggestion" + (count == 1 ? "" : "s"));
        });
    });
}"""
        },
        {
            "num": 8,
            "title": "Waste History & Date Range Filtering Screen",
            "desc": "Presents an immutable audit trail of past discarded groceries. Users can filter log rows by specific product and apply inclusive calendar date ranges (From / To).",
            "xml":
"""<!-- activity_waste_history.xml -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@color/grotrack_background">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Waste History"
        android:textSize="26sp"
        android:textStyle="bold"
        android:textColor="@color/grotrack_dark_green" />

    <Spinner
        android:id="@+id/wasteProductFilter"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <!-- Calendar Range Selectors -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        <Button
            android:id="@+id/wasteFromDateButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="From: all dates" />
        <Button
            android:id="@+id/wasteToDateButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="To: all dates" />
    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/wasteHistoryList"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
</LinearLayout>""",
            "java":
"""// WasteHistoryActivity.java excerpt
public class WasteHistoryActivity extends AppCompatActivity {
    private final ExecutorService executor = 
        Executors.newSingleThreadExecutor();
    private GroTrackDatabase database;
    private WasteHistoryAdapter adapter;
    private Spinner productFilter;
    private LocalDate fromDate, toDate;
    private List<ProductOption> productOptions = new ArrayList<>();

    private void chooseDate(boolean isFrom) {
        LocalDate initial = isFrom ? (fromDate == null ? LocalDate.now() : fromDate)
                                   : (toDate == null ? LocalDate.now() : toDate);
        new DatePickerDialog(this, (picker, year, month, day) -> {
            LocalDate chosen = LocalDate.of(year, month + 1, day);
            LocalDate newFrom = isFrom ? chosen : fromDate;
            LocalDate newTo = isFrom ? toDate : chosen;
            if (newFrom != null && newTo != null && newFrom.isAfter(newTo)) {
                Toast.makeText(this, "Start date cannot exceed end date", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
            if (isFrom) fromDate = chosen; else toDate = chosen;
            updateDateLabels();
            loadHistory();
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
    }

    private void loadHistory() {
        if (productOptions.isEmpty()) return;
        long start = fromDate == null ? 0 :
            fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = toDate == null ? 0 :
            toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Object selected = productFilter.getSelectedItem();
        Long productId = selected instanceof ProductOption ? 
            ((ProductOption) selected).id : null;

        executor.execute(() -> {
            List<HistoryItem> data = database.stockHistoryDao()
                .getWasteHistory(start, end, productId);
            runOnUiThread(() -> adapter.submit(data));
        });
    }
}"""
        },
        {
            "num": 9,
            "title": "Consumption & Waste Analytics Insights Screen",
            "desc": "Calculates household waste metrics across 7, 30, 90 day, and all-time windows. Groups waste by physical measurement unit to avoid conflating incompatible units, and ranks the most wasted products.",
            "xml":
"""<!-- activity_insights.xml -->
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:background="@color/grotrack_background">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="18dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="GroTrack Insights"
            android:textSize="26sp"
            android:textStyle="bold"
            android:textColor="@color/grotrack_dark_green" />

        <Spinner
            android:id="@+id/insightPeriodFilter"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

        <TextView
            android:id="@+id/insightWasteRecordCount"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#FFF4D8"
            android:padding="14dp"
            android:text="Waste records: 0"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/insightWasteQuantity"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="12dp"
            android:background="@color/grotrack_card"
            android:text="Wasted quantity by unit" />

        <TextView
            android:id="@+id/insightTopWasted"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="12dp"
            android:background="@color/grotrack_card"
            android:text="Most frequently wasted products" />
    </LinearLayout>
</ScrollView>""",
            "java":
"""// InsightsActivity.java - summarize
private Summary summarize(List<Product> products, List<InventoryItem> batches, List<HistoryItem> waste) {
    int expiring = 0, expired = 0, low = 0, activeProducts = 0;
    Map<Long, Double> quantities = new HashMap<>();
    for (Product product : products) if (!product.archived) activeProducts++;

    for (InventoryItem batch : batches) {
        if (batch.archived) continue;
        quantities.put(batch.productId, 
            quantities.getOrDefault(batch.productId, 0d) + batch.remainingQuantity);
        if (batch.remainingQuantity <= 0) continue;
        try {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(batch.expiryDate));
            if (days < 0) expired++; else if (days <= 3) expiring++;
        } catch (Exception ignored) {}
    }
    for (Product p : products) {
        if (!p.archived && quantities.getOrDefault(p.id, 0d) < p.minimumStock) low++;
    }

    Map<String, BigDecimal> units = new TreeMap<>();
    Map<String, WasteStat> productsWasted = new HashMap<>();
    for (HistoryItem item : waste) {
        String unit = item.unit == null ? "unit not set" : item.unit;
        BigDecimal quantity = BigDecimal.valueOf(item.quantity);
        units.put(unit, units.getOrDefault(unit, BigDecimal.ZERO).add(quantity));
        String name = item.productName == null ? "Removed product" : item.productName;
        String key = name.toLowerCase(Locale.ROOT) + "\\0" + unit.toLowerCase(Locale.ROOT);
        WasteStat stat = productsWasted.computeIfAbsent(key, k -> new WasteStat(name, unit));
        stat.count++;
        stat.quantity = stat.quantity.add(quantity);
    }
    List<WasteStat> ranked = new ArrayList<>(productsWasted.values());
    ranked.sort((l, r) -> {
        int cmp = Integer.compare(r.count, l.count);
        return cmp != 0 ? cmp : r.quantity.compareTo(l.quantity);
    });
    return new Summary(activeProducts, expiring, expired, low, waste.size(), units, ranked);
}"""
        },
        {
            "num": 10,
            "title": "Background Expiry Worker & System Notifications",
            "desc": "Scheduled via WorkManager to run daily periodic checks in the background. Queries the local Room database for batches expiring within 3 days or already expired, and triggers notifications via Android notification channels.",
            "xml":
"""<!-- AndroidManifest.xml & Notification Configuration -->
<manifest
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Android 13+ Runtime Post Notification Permission -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.GroTrack">

        <!-- Notification Channel Configuration Strings -->
        <!-- Channel ID: grotrack_expiry_channel -->
        <!-- Channel Name: Expiry Alerts -->
        <!-- Channel Importance: IMPORTANCE_DEFAULT -->

        <activity
            android:name=".DashboardActivity"
            android:exported="true"
            android:theme="@style/Theme.GroTrack.Starting">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>""",
            "java":
"""// ExpiryCheckWorker.java - doWork
public class ExpiryCheckWorker extends Worker {
    public static final String KEY_TEST_RUN = "testRun";

    public ExpiryCheckWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull @Override
    public Result doWork() {
        try {
            List<InventoryItem> items = GroTrackDatabase.getInstance(getApplicationContext())
                .batchDao().getInventory();
            LocalDate today = LocalDate.now();
            int expiring = 0, expired = 0;
            List<String> examples = new ArrayList<>();

            for (InventoryItem item : items) {
                if (item.archived || item.remainingQuantity <= 0 || item.expiryDate == null) continue;
                LocalDate expiry = LocalDate.parse(item.expiryDate);
                long days = ChronoUnit.DAYS.between(today, expiry);
                if (days < 0) {
                    expired++;
                    if (examples.size() < 3) examples.add(item.name + " (expired)");
                } else if (days <= 3) {
                    expiring++;
                    if (examples.size() < 3) examples.add(item.name + (days == 0 ? " (today)" : " (" + days + "d)"));
                }
            }

            boolean testRun = getInputData().getBoolean(KEY_TEST_RUN, false);
            if (expiring + expired == 0 && !testRun) return Result.success();
            if (!ExpiryNotifications.canNotify(getApplicationContext())) return Result.success();

            String title = expiring + expired == 0 ? "GroTrack expiry check" : "Groceries need attention";
            String message = expiring + " expiring soon, " + expired + " expired with stock.";
            if (!examples.isEmpty()) message += " " + String.join(", ", examples);
            ExpiryNotifications.show(getApplicationContext(), title, message);
            return Result.success();
        } catch (Exception ex) {
            return Result.retry();
        }
    }
}"""
        }
    ]

    for f_idx, feat in enumerate(features):
        add_p(doc, f"Feature / Screen {feat['num']}: {feat['title']}", bold=True, space_before=10, space_after=2)
        add_p(doc, feat['desc'], italic=False, space_before=0, space_after=6)

        # 2-Partition Table (Column 1: XML, Column 2: Java)
        table = doc.add_table(rows=2, cols=2)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        set_table_borders(table, color="B0B0B0", sz="4", val="single")

        # Set column widths (3.5 inches each, total 7.0 inches)
        for row in table.rows:
            row.cells[0].width = Inches(3.5)
            row.cells[1].width = Inches(3.5)

        # Header Row
        hdr_0 = table.cell(0, 0)
        hdr_1 = table.cell(0, 1)
        set_cell_shading(hdr_0, "E8E8E8")
        set_cell_shading(hdr_1, "E8E8E8")
        set_cell_margins(hdr_0, top=100, bottom=100, left=140, right=140)
        set_cell_margins(hdr_1, top=100, bottom=100, left=140, right=140)

        p_h0 = hdr_0.paragraphs[0]
        p_h0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_h0.paragraph_format.space_before = Pt(2)
        p_h0.paragraph_format.space_after = Pt(2)
        r_h0 = p_h0.add_run("Partition 1: XML Layout Code")
        set_run_font(r_h0, bold=True)

        p_h1 = hdr_1.paragraphs[0]
        p_h1.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_h1.paragraph_format.space_before = Pt(2)
        p_h1.paragraph_format.space_after = Pt(2)
        r_h1 = p_h1.add_run("Partition 2: Java Implementation Code")
        set_run_font(r_h1, bold=True)

        # Code Row
        cell_xml = table.cell(1, 0)
        cell_java = table.cell(1, 1)
        set_cell_margins(cell_xml, top=80, bottom=80, left=120, right=120)
        set_cell_margins(cell_java, top=80, bottom=80, left=120, right=120)

        # Populate XML cell
        p_xml = cell_xml.paragraphs[0]
        p_xml.paragraph_format.space_before = Pt(0)
        p_xml.paragraph_format.space_after = Pt(0)
        p_xml.paragraph_format.line_spacing = 1.05
        r_xml = p_xml.add_run(feat['xml'])
        set_run_font(r_xml, bold=False, italic=False)

        # Populate Java cell
        p_java = cell_java.paragraphs[0]
        p_java.paragraph_format.space_before = Pt(0)
        p_java.paragraph_format.space_after = Pt(0)
        p_java.paragraph_format.line_spacing = 1.05
        r_java = p_java.add_run(feat['java'])
        set_run_font(r_java, bold=False, italic=False)

        # Add spacing before Screenshot Box
        add_p(doc, "", space_before=4, space_after=4)

        # Screenshot Placeholder Box
        box_table = doc.add_table(rows=1, cols=1)
        box_table.alignment = WD_TABLE_ALIGNMENT.CENTER
        box_cell = box_table.cell(0, 0)
        box_cell.width = Inches(7.0)
        set_placeholder_box_borders(box_table, color="777777", sz="6")
        set_cell_shading(box_cell, "FAFAFA")
        set_cell_margins(box_cell, top=280, bottom=280, left=200, right=200)

        # Add text inside screenshot placeholder
        bp = box_cell.paragraphs[0]
        bp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        bp.paragraph_format.space_before = Pt(20)
        bp.paragraph_format.space_after = Pt(4)
        r_box1 = bp.add_run(f"[ Space Reserved for Screenshot: {feat['title']} ]")
        set_run_font(r_box1, bold=True)

        bp2 = box_cell.add_paragraph()
        bp2.alignment = WD_ALIGN_PARAGRAPH.CENTER
        bp2.paragraph_format.space_before = Pt(4)
        bp2.paragraph_format.space_after = Pt(20)
        r_box2 = bp2.add_run("Paste the screenshot image of this screen or dialog directly here.")
        set_run_font(r_box2, italic=True)

        # Clean separator or page break after each feature
        if f_idx < len(features) - 1:
            doc.add_page_break()

    doc.save(output_path)
    print(f"Documentation successfully created at: {output_path}")

if __name__ == "__main__":
    out_file = r"c:\Users\Administrator\AndroidStudioProjects\GroTrack\GroTrack_Documentation.docx"
    build_grotrack_documentation(out_file)
