package com.example.grotrack.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull public String name = "";
    @NonNull public String category = "Other";
    @NonNull public String unit = "piece";
    public double minimumStock;
    public boolean archived;
}
