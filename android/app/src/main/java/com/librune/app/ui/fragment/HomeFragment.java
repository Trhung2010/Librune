package com.librune.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.librune.app.R;
import com.librune.app.api.ApiClient;
import com.librune.app.model.Book;
import com.librune.app.ui.adapter.BookAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeHome;
    private BookAdapter featuredAdapter;
    private BookAdapter newestAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        swipeHome = view.findViewById(R.id.swipeHome);
        RecyclerView rvFeatured = view.findViewById(R.id.rvFeaturedBooks);
        RecyclerView rvNewest = view.findViewById(R.id.rvNewestBooks);

        rvFeatured.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredAdapter = new BookAdapter(requireContext(), true);
        rvFeatured.setAdapter(featuredAdapter);

        rvNewest.setLayoutManager(new LinearLayoutManager(requireContext()));
        newestAdapter = new BookAdapter(requireContext(), false);
        rvNewest.setAdapter(newestAdapter);

        swipeHome.setOnRefreshListener(this::loadData);
        loadData();
        return view;
    }

    private void loadData() {
        swipeHome.setRefreshing(true);
        ApiClient.getService(requireContext()).getFeaturedBooks().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (isAdded() && response.isSuccessful()) {
                    featuredAdapter.setBooks(response.body());
                }
                swipeHome.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                if (isAdded()) {
                    swipeHome.setRefreshing(false);
                    Toast.makeText(requireContext(), "Lỗi tải sách nổi bật", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ApiClient.getService(requireContext()).getNewestBooks().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (isAdded() && response.isSuccessful()) {
                    newestAdapter.setBooks(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                // Handled by swipeHome refresh state
            }
        });
    }
}
