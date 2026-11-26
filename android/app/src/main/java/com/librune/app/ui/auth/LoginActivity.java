package com.librune.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etServerUrl;
    private SessionManager sessionManager;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            openMain();
            return;
        }

        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etServerUrl = findViewById(R.id.etServerUrl);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        etServerUrl.setText(sessionManager.getBaseUrl());

        btnLogin.setOnClickListener(v -> doLogin());
        tvGoRegister.setOnClickListener(v -> {
            saveServerUrl();
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void saveServerUrl() {
        String url = etServerUrl.getText() != null ? etServerUrl.getText().toString() : "";
        sessionManager.setBaseUrl(url);
        ApiClient.reset();
    }

    private void doLogin() {
        saveServerUrl();
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        ApiClient.getService(this).login(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = String.valueOf(response.body().get("token"));
                    User user = gson.fromJson(gson.toJson(response.body().get("user")), User.class);
                    sessionManager.saveSession(token, user);
                    openMain();
                } else {
                    Toast.makeText(LoginActivity.this,
                            ErrorUtils.extractMessage(response, "Đăng nhập thất bại"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Không kết nối được server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
