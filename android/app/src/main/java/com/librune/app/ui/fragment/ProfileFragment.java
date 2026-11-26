package com.librune.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.librune.app.R;
import com.librune.app.api.ApiClient;
import com.librune.app.model.User;
import com.librune.app.ui.auth.LoginActivity;
import com.librune.app.util.BorrowCartManager;
import com.librune.app.util.ErrorUtils;
import com.librune.app.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUsername;
    private TextInputEditText etFullName, etEmail, etPhone, etAddress, etOldPassword, etNewPassword;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        sessionManager = new SessionManager(requireContext());

        tvUsername = view.findViewById(R.id.tvProfileUsername);
        etFullName = view.findViewById(R.id.etProfFullName);
        etEmail = view.findViewById(R.id.etProfEmail);
        etPhone = view.findViewById(R.id.etProfPhone);
        etAddress = view.findViewById(R.id.etProfAddress);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);

        Button btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        bindUser(sessionManager.getUser());
        fetchLatestProfile();

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnLogout.setOnClickListener(v -> logout());
        return view;
    }

    private void fetchLatestProfile() {
        ApiClient.getService(requireContext()).getMyProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    sessionManager.updateUser(response.body());
                    bindUser(response.body());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    private void bindUser(User user) {
        if (user == null) return;
        tvUsername.setText("Tài khoản: " + user.getUsername());
        etFullName.setText(user.getFullName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhone());
        etAddress.setText(user.getAddress());
    }

    private void updateProfile() {
        User updates = new User();
        updates.setFullName(textOf(etFullName));
        updates.setEmail(textOf(etEmail));
        updates.setPhone(textOf(etPhone));
        updates.setAddress(textOf(etAddress));

        ApiClient.getService(requireContext()).updateMyProfile(updates).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.updateUser(response.body());
                    Toast.makeText(requireContext(), "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), ErrorUtils.extractMessage(response, "Cập nhật thất bại"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changePassword() {
        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", textOf(etOldPassword));
        body.put("newPassword", textOf(etNewPassword));

        ApiClient.getService(requireContext()).changePassword(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    etOldPassword.setText("");
                    etNewPassword.setText("");
                    Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), ErrorUtils.extractMessage(response, "Đổi mật khẩu thất bại"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        ApiClient.getService(requireContext()).logout().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                finishLogout();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                finishLogout();
            }
        });
    }

    private void finishLogout() {
        if (!isAdded()) return;
        BorrowCartManager.getInstance().clear();
        sessionManager.clearSession();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
