package com.example.grotrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grotrack.data.ShoppingListItem;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.Holder> {
    interface Actions { void purchase(ShoppingListItem item); void edit(ShoppingListItem item); void delete(ShoppingListItem item); }
    private final List<ShoppingListItem> items=new ArrayList<>();
    private final Actions actions;
    ShoppingListAdapter(Actions actions) { this.actions=actions; }
    void submit(List<ShoppingListItem> data) { items.clear(); items.addAll(data); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type) { return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping,parent,false)); }
    @Override public void onBindViewHolder(@NonNull Holder h,int position) {
        ShoppingListItem item=items.get(position); h.name.setText(item.itemName); h.meta.setText(item.quantity+" "+item.unit+(item.purchased?" • Purchased":" • Added "+android.text.format.DateFormat.format("yyyy-MM-dd",item.createdAt)));
        h.purchase.setText(item.purchased?"Purchased":"Record purchase"); h.purchase.setEnabled(!item.purchased); h.edit.setEnabled(!item.purchased);
        h.purchase.setOnClickListener(v -> actions.purchase(item)); h.edit.setOnClickListener(v -> actions.edit(item)); h.delete.setOnClickListener(v -> actions.delete(item));
    }
    @Override public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        TextView name,meta; Button purchase,edit,delete;
        Holder(View v) { super(v); name=v.findViewById(R.id.shoppingItemName); meta=v.findViewById(R.id.shoppingItemMeta); purchase=v.findViewById(R.id.shoppingPurchaseButton); edit=v.findViewById(R.id.shoppingEditButton); delete=v.findViewById(R.id.shoppingDeleteButton); }
    }
}
