package com.example.grotrack.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "grocery_batches",
        foreignKeys = @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId", onDelete = ForeignKey.RESTRICT),
        indices = {@Index("productId")})
public class GroceryBatch {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long productId;
    public double purchasedQuantity;
    public double remainingQuantity;
    public String purchaseDate;
    public String expiryDate;
    public Double purchasePrice;
}
