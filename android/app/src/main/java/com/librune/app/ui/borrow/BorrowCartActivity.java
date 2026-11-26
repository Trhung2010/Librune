package com.librune.app.ui.borrow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.librune.app.R;
import com.librune.app.api.ApiClient;
import com.librune.app.model.BorrowRequest;
import com.librune.app.ui.adapter.CartAdapter;
import com.librune.app.util.BorrowCartManager;
import com.librune.app.util.ErrorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BorrowCartActivity extends AppCompatActivity {

    private CartAdapter adapter;
    private TextInputEditText etBorrowNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_cart);

        RecyclerView rvCart = findViewById(R.id.rvCart);
        etBorrowNote = findViewById(R.id.etBorrowNote);
        Button btnSubmit = findViewById(R.id.btnSubmitBorrow);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(bookId -> {
            BorrowCartManager.getInstance().removeBook(bookId);
            adapter.setItems(BorrowCartManager.getInstance().getItems());
        });
        rvCart.setAdapter(adapter);
        adapter.setItems(BorrowCartManager.getInstance().getItems());

        btnSubmit.setOnClickListener(v -> submitBorrowRequest());
    }

    private void submitBorrowRequest() {
        List<BorrowCartManager.CartItem> cartItems = BorrowCartManager.getInstance().getItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ mượn đang trống", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> itemsPayload = new ArrayList<>();
        for (BorrowCartManager.CartItem ci : cartItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("bookId", ci.book.getId());
            row.put("quantity", ci.quantity);
            itemsPayload.add(row);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("items", itemsPayload);
        body.put("note", etBorrowNote.getText() != null ? etBorrowNote.getText().toString().trim() : "");

        ApiClient.getService(this).createBorrowRequest(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BorrowRequest> call, Response<BorrowRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BorrowCartManager.getInstance().clear();
                    Toast.makeText(BorrowCartActivity.this,
                            "Đã gửi yêu cầu mượn #" + response.body().getId(), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(BorrowCartActivity.this,
                            ErrorUtils.extractMessage(response, "Không gửi được yêu cầu"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BorrowRequest> call, Throwable t) {
                Toast.makeText(BorrowCartActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
