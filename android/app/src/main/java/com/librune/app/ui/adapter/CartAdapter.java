package com.librune.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.librune.app.R;
import com.librune.app.util.BorrowCartManager;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnRemoveListener {
        void onRemove(Long bookId);
    }

    private final OnRemoveListener removeListener;
    private List<BorrowCartManager.CartItem> items = new ArrayList<>();

    public CartAdapter(OnRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setItems(List<BorrowCartManager.CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_book, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        BorrowCartManager.CartItem item = items.get(position);
        holder.tvTitle.setText(item.book.getTitle());
        holder.tvQty.setText("Số lượng mượn: " + item.quantity);
        holder.btnRemove.setOnClickListener(v -> removeListener.onRemove(item.book.getId()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvQty;
        final Button btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCartBookTitle);
            tvQty = itemView.findViewById(R.id.tvCartBookQty);
            btnRemove = itemView.findViewById(R.id.btnRemoveCartItem);
        }
    }
}
