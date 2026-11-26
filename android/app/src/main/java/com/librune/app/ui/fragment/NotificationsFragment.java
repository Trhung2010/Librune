package com.librune.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.librune.app.R;
import com.librune.app.api.ApiClient;
import com.librune.app.model.Notification;
import com.librune.app.ui.adapter.NotificationAdapter;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private SwipeRefreshLayout swipeNotifications;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        swipeNotifications = view.findViewById(R.id.swipeNotifications);
        RecyclerView rvNotifications = view.findViewById(R.id.rvNotifications);
        Button btnReadAll = view.findViewById(R.id.btnReadAllNotifications);

        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(this::markOneRead);
        rvNotifications.setAdapter(adapter);

        swipeNotifications.setOnRefreshListener(this::loadNotifications);
        btnReadAll.setOnClickListener(v -> markAllRead());

        loadNotifications();
        return view;
    }

    private void loadNotifications() {
        swipeNotifications.setRefreshing(true);
        ApiClient.getService(requireContext()).getMyNotifications().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (isAdded()) {
                    swipeNotifications.setRefreshing(false);
                    if (response.isSuccessful()) {
                        adapter.setNotifications(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                if (isAdded()) {
                    swipeNotifications.setRefreshing(false);
                    Toast.makeText(requireContext(), "Không tải được thông báo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void markOneRead(Notification notification) {
        if (Boolean.TRUE.equals(notification.getRead())) return;
        ApiClient.getService(requireContext()).markNotificationRead(notification.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                if (isAdded() && response.isSuccessful()) {
                    loadNotifications();
                }
            }

            @Override
            public void onFailure(Call<Notification> call, Throwable t) {
            }
        });
    }

    private void markAllRead() {
        ApiClient.getService(requireContext()).markAllNotificationsRead().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (isAdded() && response.isSuccessful()) {
                    loadNotifications();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
            }
        });
    }
}
