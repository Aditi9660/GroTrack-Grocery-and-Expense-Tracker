package com.example.grotrack.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_history",
        foreignKeys = {
                @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId", onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = GroceryBatch.class, parentColumns = "id", childColumns = "batchId", onDelete = ForeignKey.SET_NULL)
        }, indices = {@Index("productId"), @Index("batchId"), @Index("timestamp")})
public class StockHistory {
    @PrimaryKey(autoGenerate = true) public long id;
    public Long productId;
    public Long batchId;
    public String productName;
    public String unit;
    public String actionType;
    public double quantity;
    public String reason;
    public long timestamp;
}
