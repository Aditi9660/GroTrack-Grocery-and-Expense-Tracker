package com.example.grotrack.data;

import androidx.room.ColumnInfo;

public class InventoryItem {
    @ColumnInfo(name = "productId") public long productId;
    @ColumnInfo(name = "name") public String name;
    @ColumnInfo(name = "category") public String category;
    @ColumnInfo(name = "unit") public String unit;
    @ColumnInfo(name = "minimumStock") public double minimumStock;
    @ColumnInfo(name = "archived") public boolean archived;
    @ColumnInfo(name = "batchId") public long batchId;
    @ColumnInfo(name = "purchasedQuantity") public double purchasedQuantity;
    @ColumnInfo(name = "remainingQuantity") public double remainingQuantity;
    @ColumnInfo(name = "purchaseDate") public String purchaseDate;
    @ColumnInfo(name = "expiryDate") public String expiryDate;
    @ColumnInfo(name = "purchasePrice") public Double purchasePrice;
}
