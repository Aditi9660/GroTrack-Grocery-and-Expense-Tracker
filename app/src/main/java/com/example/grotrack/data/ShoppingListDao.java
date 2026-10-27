package com.example.grotrack.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ShoppingListDao {
    @Insert long insert(ShoppingListItem item);
    @Update void update(ShoppingListItem item);
    @Delete void delete(ShoppingListItem item);
    @Query("SELECT * FROM shopping_list_items ORDER BY purchased ASC, createdAt DESC") List<ShoppingListItem> getAll();
    @Query("SELECT * FROM shopping_list_items WHERE id = :id LIMIT 1") ShoppingListItem getById(long id);
    @Query("SELECT * FROM shopping_list_items WHERE productId = :productId AND purchased = 0 LIMIT 1") ShoppingListItem findUnpurchasedForProduct(long productId);
}
