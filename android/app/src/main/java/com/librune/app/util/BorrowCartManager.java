package com.librune.app.util;

import com.librune.app.model.Book;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds books selected by the reader before submitting a borrow request.
 */
public class BorrowCartManager {

    private static final BorrowCartManager INSTANCE = new BorrowCartManager();
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public static class CartItem {
        public final Book book;
        public int quantity;

        public CartItem(Book book, int quantity) {
            this.book = book;
            this.quantity = quantity;
        }
    }

    private BorrowCartManager() {}

    public static BorrowCartManager getInstance() {
        return INSTANCE;
    }

    public void addBook(Book book) {
        if (book == null || book.getId() == null) return;
        CartItem existing = items.get(book.getId());
        if (existing != null) {
            int max = book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 1;
            if (existing.quantity < max) {
                existing.quantity++;
            }
        } else {
            items.put(book.getId(), new CartItem(book, 1));
        }
    }

    public void removeBook(Long bookId) {
        items.remove(bookId);
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public int getTotalCount() {
        int sum = 0;
        for (CartItem item : items.values()) {
            sum += item.quantity;
        }
        return sum;
    }

    public void clear() {
        items.clear();
    }
}
