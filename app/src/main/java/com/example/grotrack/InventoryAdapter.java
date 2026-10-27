package com.example.grotrack;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grotrack.data.InventoryItem;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.Holder> {
    public interface Actions { void addBatch(InventoryItem item); void edit(InventoryItem item); void delete(InventoryItem item); void consume(InventoryItem item); void waste(InventoryItem item); }
    private final List<InventoryItem> rows = new ArrayList<>();
    private final Actions actions;
    public InventoryAdapter(Actions actions) { this.actions = actions; }
    public void submit(List<InventoryItem> data) { rows.clear(); rows.addAll(data); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false)); }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        InventoryItem row = rows.get(position);
        h.name.setText(row.name);
        h.meta.setText(row.category + " • " + row.remainingQuantity + " " + row.unit + " remaining (of " + row.purchasedQuantity + ")");
        h.dates.setText("Bought " + row.purchaseDate + "  •  Expires " + row.expiryDate);
        String status = status(row.expiryDate);
        h.status.setText(status);
        h.status.setTextColor("Expired".equals(status) ? Color.rgb(183, 45, 45) : "Expiring Soon".equals(status) ? Color.rgb(168, 105, 0) : Color.rgb(38, 117, 67));
        h.addBatch.setOnClickListener(v -> actions.addBatch(row));
        h.edit.setOnClickListener(v -> actions.edit(row));
        h.delete.setOnClickListener(v -> actions.delete(row));
        h.consume.setEnabled(row.remainingQuantity > 0); h.waste.setEnabled(row.remainingQuantity > 0);
        h.consume.setOnClickListener(v -> actions.consume(row)); h.waste.setOnClickListener(v -> actions.waste(row));
    }
    private String status(String date) {
        try { long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date)); return days < 0 ? "Expired" : days <= 3 ? "Expiring Soon" : "Fresh"; }
        catch (Exception ex) { return "Date unavailable"; }
    }
    @Override public int getItemCount() { return rows.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        TextView name, meta, dates, status; Button addBatch, edit, delete, consume, waste;
        Holder(View v) { super(v); name=v.findViewById(R.id.itemName); meta=v.findViewById(R.id.itemMeta); dates=v.findViewById(R.id.itemDates); status=v.findViewById(R.id.itemStatus); addBatch=v.findViewById(R.id.addBatchButton); edit=v.findViewById(R.id.editProductButton); delete=v.findViewById(R.id.deleteProductButton); consume=v.findViewById(R.id.consumeBatchButton); waste=v.findViewById(R.id.wasteBatchButton); }
    }
}
