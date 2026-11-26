package com.librune.service;

import com.librune.entity.Category;
import com.librune.repository.BookRepository;
import com.librune.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Page<Category> searchPage(String keyword, int page, int size) {
        Pageable pageable = PaginationSupport.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        String cleanKeyword = keyword != null ? keyword.trim() : null;
        return categoryRepository.searchPage(cleanKeyword, pageable);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục #" + id));
    }

    @Transactional
    public Category save(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        String trimmedName = category.getName().trim();
        category.setName(trimmedName);
        boolean duplicate = category.getId() == null
                ? categoryRepository.existsByNameIgnoreCase(trimmedName)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, category.getId());
        if (duplicate) {
            throw new IllegalArgumentException("Danh mục '" + trimmedName + "' đã tồn tại");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(Long id) {
        if (bookRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Không thể xóa danh mục đang có sách");
        }
        categoryRepository.deleteById(id);
    }
}
