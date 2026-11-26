package com.librune.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.librune.app.R;
import com.librune.app.api.ApiClient;
import com.librune.app.model.Book;
import com.librune.app.model.Category;
import com.librune.app.ui.adapter.BookAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText etKeyword;
    private Spinner spinnerCategory;
    private CheckBox cbAvailableOnly;
    private BookAdapter adapter;
    private final List<Category> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        etKeyword = view.findViewById(R.id.etSearchKeyword);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        cbAvailableOnly = view.findViewById(R.id.cbAvailableOnly);
        Button btnSearch = view.findViewById(R.id.btnSearch);
        RecyclerView rvSearch = view.findViewById(R.id.rvSearchBooks);

        rvSearch.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookAdapter(requireContext(), false);
        rvSearch.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> performSearch());
        cbAvailableOnly.setOnCheckedChangeListener((buttonView, isChecked) -> performSearch());

        loadCategories();
        performSearch();
        return view;
    }

    private void loadCategories() {
        ApiClient.getService(requireContext()).getCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                categories.clear();
                Category all = new Category();
                all.setId(null);
                all.setName("Tất cả danh mục");
                categories.add(all);
                categories.addAll(response.body());

                ArrayAdapter<Category> spinnerAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, categories);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(spinnerAdapter);
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
            }
        });
    }

    private void performSearch() {
        String keyword = etKeyword.getText() != null ? etKeyword.getText().toString().trim() : "";
        Long categoryId = null;
        if (spinnerCategory.getSelectedItem() instanceof Category selected) {
            categoryId = selected.getId();
        }
        boolean availableOnly = cbAvailableOnly.isChecked();

        ApiClient.getService(requireContext()).searchBooks(keyword, categoryId, availableOnly).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (isAdded() && response.isSuccessful()) {
                    adapter.setBooks(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi tìm kiếm sách", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
