package com.librune.app.ui.book;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.librune.app.R;
import com.librune.app.api.ApiClient;
import com.librune.app.model.Book;
import com.librune.app.ui.borrow.BorrowCartActivity;
import com.librune.app.util.BorrowCartManager;
import com.librune.app.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";

    private ImageView ivCover;
    private TextView tvTitle, tvAuthors, tvMeta, tvAvailability, tvDescription;
    private Button btnAddToCart, btnBorrowNow;
    private Book currentBook;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        sessionManager = new SessionManager(this);

        ivCover = findViewById(R.id.ivDetailCover);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthors = findViewById(R.id.tvDetailAuthors);
        tvMeta = findViewById(R.id.tvDetailMeta);
        tvAvailability = findViewById(R.id.tvDetailAvailability);
        tvDescription = findViewById(R.id.tvDetailDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBorrowNow = findViewById(R.id.btnBorrowNow);

        long bookId = getIntent().getLongExtra(EXTRA_BOOK_ID, -1);
        if (bookId <= 0) {
            finish();
            return;
        }

        loadBook(bookId);

        btnAddToCart.setOnClickListener(v -> {
            if (currentBook != null && canBorrow(currentBook)) {
                BorrowCartManager.getInstance().addBook(currentBook);
                Toast.makeText(this, "Đã thêm vào giỏ mượn", Toast.LENGTH_SHORT).show();
            }
        });

        btnBorrowNow.setOnClickListener(v -> {
            if (currentBook != null && canBorrow(currentBook)) {
                BorrowCartManager.getInstance().addBook(currentBook);
                startActivity(new Intent(this, BorrowCartActivity.class));
            }
        });
    }

    private boolean canBorrow(Book book) {
        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
            Toast.makeText(this, "Sách hiện đã hết bản có sẵn", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadBook(long bookId) {
        ApiClient.getService(this).getBookDetail(bookId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindBook(response.body());
                } else {
                    Toast.makeText(BookDetailActivity.this, "Không tải được chi tiết sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindBook(Book book) {
        this.currentBook = book;
        tvTitle.setText(book.getTitle());
        tvAuthors.setText("Tác giả: " + book.getAuthorsText());

        String cat = book.getCategory() != null ? book.getCategory().getName() : "-";
        String pub = book.getPublisher() != null ? book.getPublisher() : "-";
        String year = book.getPublishYear() != null ? String.valueOf(book.getPublishYear()) : "-";
        String isbn = book.getIsbn() != null ? book.getIsbn() : "-";
        tvMeta.setText("Danh mục: " + cat + "\nNXB: " + pub + " (" + year + ")\nISBN: " + isbn);

        int avail = book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0;
        int total = book.getTotalQuantity() != null ? book.getTotalQuantity() : 0;
        if (avail > 0) {
            tvAvailability.setText("Tình trạng: Còn " + avail + " / " + total + " cuốn");
            tvAvailability.setTextColor(ContextCompat.getColor(this, R.color.librune_success));
        } else {
            tvAvailability.setText("Tình trạng: Hết sách (0 / " + total + ")");
            tvAvailability.setTextColor(ContextCompat.getColor(this, R.color.librune_danger));
            btnAddToCart.setEnabled(false);
            btnBorrowNow.setEnabled(false);
        }

        tvDescription.setText(book.getDescription() != null ? book.getDescription() : "Chưa có mô tả.");
        String coverUrl = sessionManager.resolveImageUrl(book.getCoverImage());
        if (coverUrl != null) {
            Glide.with(this).load(coverUrl).placeholder(android.R.drawable.ic_menu_gallery).into(ivCover);
        } else {
            ivCover.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }
}
