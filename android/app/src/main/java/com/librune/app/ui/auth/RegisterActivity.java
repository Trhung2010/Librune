package com.librune.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.librune.app.R;
import com.librune.app.api.ApiClient;
import com.librune.app.model.User;
import com.librune.app.ui.main.MainActivity;
import com.librune.app.util.ErrorUtils;
import com.librune.app.util.SessionManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etFullName, etEmail, etPhone, etAddress;
    private SessionManager sessionManager;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sessionManager = new SessionManager(this);

        etUsername = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);
        etFullName = findViewById(R.id.etRegFullName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etRegPhone);
        etAddress = findViewById(R.id.etRegAddress);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        User user = new User();
        user.setUsername(textOf(etUsername));
        user.setPassword(textOf(etPassword));
        user.setFullName(textOf(etFullName));
        user.setEmail(textOf(etEmail));
        user.setPhone(textOf(etPhone));
        user.setAddress(textOf(etAddress));

        ApiClient.getService(this).register(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = String.valueOf(response.body().get("token"));
                    User created = gson.fromJson(gson.toJson(response.body().get("user")), User.class);
                    sessionManager.saveSession(token, created);
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this,
                            ErrorUtils.extractMessage(response, "Đăng ký thất bại"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
