package com.librune.app.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.librune.app.R;
import com.librune.app.model.Book;
import com.librune.app.ui.book.BookDetailActivity;
import com.librune.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final boolean horizontal;
    private final SessionManager sessionManager;
    private List<Book> books = new ArrayList<>();

    public BookAdapter(Context context, boolean horizontal) {
        this.horizontal = horizontal;
        this.sessionManager = new SessionManager(context);
    }

    public void setBooks(List<Book> books) {
        this.books = books != null ? books : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = horizontal ? R.layout.item_book_horizontal : R.layout.item_book_list;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthorsText());
        if (holder.tvCategory != null && book.getCategory() != null) {
            holder.tvCategory.setText(book.getCategory().getName());
        }

        int available = book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0;
        if (available > 0) {
            holder.tvStock.setText("Còn " + available + " cuốn");
            holder.tvStock.setTextColor(ContextCompat.getColor(ctx, R.color.librune_success));
        } else {
            holder.tvStock.setText("Hết sách");
            holder.tvStock.setTextColor(ContextCompat.getColor(ctx, R.color.librune_danger));
        }

        String coverUrl = sessionManager.resolveImageUrl(book.getCoverImage());
        if (coverUrl != null) {
            Glide.with(ctx).load(coverUrl).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivCover;
        final TextView tvTitle;
        final TextView tvAuthor;
        final TextView tvCategory;
        final TextView tvStock;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivBookCover);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvCategory = itemView.findViewById(R.id.tvBookCategory);
            tvStock = itemView.findViewById(R.id.tvBookStock);
        }
    }
}
