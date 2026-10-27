package com.example.grotrack.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GroceryBatchDao {
    @Insert long insert(GroceryBatch batch);
    @Query("SELECT * FROM grocery_batches WHERE id = :id LIMIT 1") GroceryBatch getById(long id);
    @Query("UPDATE grocery_batches SET remainingQuantity = remainingQuantity - :quantity WHERE id = :id AND remainingQuantity >= :quantity") int subtractStock(long id, double quantity);
    @Query("SELECT p.id AS productId, p.name, p.category, p.unit, p.minimumStock, p.archived, b.id AS batchId, b.purchasedQuantity, b.remainingQuantity, b.purchaseDate, b.expiryDate, b.purchasePrice FROM products p INNER JOIN grocery_batches b ON p.id = b.productId ORDER BY p.name COLLATE NOCASE, b.expiryDate")
    List<InventoryItem> getInventory();
    @Query("SELECT * FROM grocery_batches WHERE productId = :productId ORDER BY expiryDate") List<GroceryBatch> getForProduct(long productId);
    @Query("DELETE FROM grocery_batches WHERE id = :id") void delete(long id);
}
