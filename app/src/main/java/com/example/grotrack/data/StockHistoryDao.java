package com.example.grotrack.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface StockHistoryDao {
    @Insert long insert(StockHistory item);
    @Query("SELECT id, productId, productName, unit, actionType, quantity, reason, timestamp FROM stock_history WHERE actionType = 'WASTED' AND (:fromTime = 0 OR timestamp >= :fromTime) AND (:toTime = 0 OR timestamp < :toTime) AND (:productId IS NULL OR productId = :productId) ORDER BY timestamp DESC")
    List<HistoryItem> getWasteHistory(long fromTime, long toTime, Long productId);
    @Query("SELECT id, productId, productName, unit, actionType, quantity, reason, timestamp FROM stock_history ORDER BY timestamp DESC")
    List<HistoryItem> getAll();
}
