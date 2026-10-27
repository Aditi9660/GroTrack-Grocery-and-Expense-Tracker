package com.example.grotrack.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Product.class, GroceryBatch.class, ShoppingListItem.class, StockHistory.class}, version = 2, exportSchema = false)
public abstract class GroTrackDatabase extends RoomDatabase {
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS shopping_list_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, productId INTEGER, itemName TEXT, quantity REAL NOT NULL, unit TEXT, purchased INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(productId) REFERENCES products(id) ON UPDATE NO ACTION ON DELETE SET NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_list_items_productId ON shopping_list_items(productId)");
            db.execSQL("CREATE TABLE IF NOT EXISTS stock_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, productId INTEGER, batchId INTEGER, productName TEXT, unit TEXT, actionType TEXT, quantity REAL NOT NULL, reason TEXT, timestamp INTEGER NOT NULL, FOREIGN KEY(productId) REFERENCES products(id) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(batchId) REFERENCES grocery_batches(id) ON UPDATE NO ACTION ON DELETE SET NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_history_productId ON stock_history(productId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_history_batchId ON stock_history(batchId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_history_timestamp ON stock_history(timestamp)");
        }
    };
    private static volatile GroTrackDatabase INSTANCE;
    public abstract ProductDao productDao();
    public abstract GroceryBatchDao batchDao();
    public abstract ShoppingListDao shoppingListDao();
    public abstract StockHistoryDao stockHistoryDao();

    public static GroTrackDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GroTrackDatabase.class) {
                if (INSTANCE == null) INSTANCE = Room.databaseBuilder(context.getApplicationContext(), GroTrackDatabase.class, "grotrack.db").addMigrations(MIGRATION_1_2).build();
            }
        }
        return INSTANCE;
    }
}
