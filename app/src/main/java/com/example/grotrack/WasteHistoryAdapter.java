package com.example.grotrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grotrack.data.HistoryItem;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WasteHistoryAdapter extends RecyclerView.Adapter<WasteHistoryAdapter.Holder> {
    private final List<HistoryItem> items=new ArrayList<>();
    void submit(List<HistoryItem> data) { items.clear(); items.addAll(data); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type) { return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waste_history,parent,false)); }
    @Override public void onBindViewHolder(@NonNull Holder h,int position) {
        HistoryItem item=items.get(position); h.name.setText(item.productName==null?"Removed product":item.productName);
        h.quantity.setText("Wasted: "+item.quantity+" "+item.unit); h.reason.setText("Reason: "+(item.reason==null||item.reason.isEmpty()?"Not specified":item.reason));
        h.time.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT, Locale.getDefault()).format(new Date(item.timestamp)));
    }
    @Override public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        TextView name,quantity,reason,time;
        Holder(View v) { super(v); name=v.findViewById(R.id.wasteProductName); quantity=v.findViewById(R.id.wasteQuantity); reason=v.findViewById(R.id.wasteReason); time=v.findViewById(R.id.wasteTimestamp); }
    }
}
