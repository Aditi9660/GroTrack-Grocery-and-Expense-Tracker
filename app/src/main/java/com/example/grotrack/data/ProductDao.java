package com.example.grotrack.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert long insert(Product product);
    @Update void update(Product product);
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE") List<Product> getAll();
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1") Product getById(long id);
    @Query("SELECT * FROM products WHERE archived = 0 AND name = :name COLLATE NOCASE AND unit = :unit COLLATE NOCASE LIMIT 1") Product findActiveByNameAndUnit(String name, String unit);
    @Query("UPDATE products SET archived = 1 WHERE id = :id") void archive(long id);
    @Query("DELETE FROM products WHERE id = :id") void delete(long id);
}
