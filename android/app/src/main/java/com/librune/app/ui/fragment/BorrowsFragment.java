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
import com.librune.app.model.BorrowRequest;
import com.librune.app.ui.adapter.BorrowRequestAdapter;
import com.librune.app.util.ErrorUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BorrowsFragment extends Fragment {

    private SwipeRefreshLayout swipeBorrows;
    private BorrowRequestAdapter adapter;
    private List<BorrowRequest> allRequests = new ArrayList<>();
    private String mode = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrows, container, false);
        swipeBorrows = view.findViewById(R.id.swipeBorrows);
        RecyclerView rvBorrows = view.findViewById(R.id.rvBorrows);
        Button btnAll = view.findViewById(R.id.btnFilterAll);
        Button btnActive = view.findViewById(R.id.btnFilterActive);
        Button btnHistory = view.findViewById(R.id.btnFilterHistory);

        rvBorrows.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BorrowRequestAdapter(this::cancelRequest);
        rvBorrows.setAdapter(adapter);

        btnAll.setOnClickListener(v -> { mode = "ALL"; applyFilter(); });
        btnActive.setOnClickListener(v -> { mode = "ACTIVE"; applyFilter(); });
        btnHistory.setOnClickListener(v -> { mode = "HISTORY"; applyFilter(); });

        swipeBorrows.setOnRefreshListener(this::loadBorrows);
        loadBorrows();
        return view;
    }

    private void loadBorrows() {
        swipeBorrows.setRefreshing(true);
        ApiClient.getService(requireContext()).getMyBorrows(null).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<BorrowRequest>> call, Response<List<BorrowRequest>> response) {
                if (!isAdded()) return;
                swipeBorrows.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allRequests = response.body();
                    applyFilter();
                }
            }

            @Override
            public void onFailure(Call<List<BorrowRequest>> call, Throwable t) {
                if (isAdded()) {
                    swipeBorrows.setRefreshing(false);
                    Toast.makeText(requireContext(), "Không tải được lịch sử mượn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyFilter() {
        if ("ALL".equals(mode)) {
            adapter.setRequests(allRequests);
            return;
        }
        List<BorrowRequest> filtered = new ArrayList<>();
        for (BorrowRequest r : allRequests) {
            String s = r.getStatus();
            boolean isActive = "PENDING".equals(s) || "APPROVED".equals(s);
            if ("ACTIVE".equals(mode) && isActive) filtered.add(r);
            else if ("HISTORY".equals(mode) && !isActive) filtered.add(r);
        }
        adapter.setRequests(filtered);
    }

    private void cancelRequest(BorrowRequest req) {
        ApiClient.getService(requireContext()).cancelBorrowRequest(req.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BorrowRequest> call, Response<BorrowRequest> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã hủy phiếu #" + req.getId(), Toast.LENGTH_SHORT).show();
                    loadBorrows();
                } else {
                    Toast.makeText(requireContext(), ErrorUtils.extractMessage(response, "Không thể hủy phiếu"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BorrowRequest> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
