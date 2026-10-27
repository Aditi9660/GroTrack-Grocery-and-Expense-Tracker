package com.example.grotrack.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_list_items",
        foreignKeys = @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId", onDelete = ForeignKey.SET_NULL),
        indices = {@Index("productId")})
public class ShoppingListItem {
    @PrimaryKey(autoGenerate = true) public long id;
    public Long productId;
    public String itemName;
    public double quantity;
    public String unit;
    public boolean purchased;
    public long createdAt;
}
