package com.librune.service;

import com.librune.entity.Author;
import com.librune.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Author> findAll() {
        return authorRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<Author> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return authorRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim());
    }

    public Page<Author> searchPage(String keyword, int page, int size) {
        Pageable pageable = PaginationSupport.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        String cleanKeyword = keyword != null ? keyword.trim() : null;
        return authorRepository.searchPage(cleanKeyword, pageable);
    }

    public Author findById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tác giả #" + id));
    }

    public Set<Author> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(authorRepository.findAllById(ids));
    }

    @Transactional
    public Author save(Author author) {
        if (author.getName() == null || author.getName().isBlank()) {
            throw new IllegalArgumentException("Tên tác giả không được để trống");
        }
        author.setName(author.getName().trim());
        return authorRepository.save(author);
    }

    @Transactional
    public void deleteById(Long id) {
        Author author = findById(id);
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            throw new IllegalStateException("Không thể xóa tác giả đang gắn với sách");
        }
        authorRepository.deleteById(id);
    }
}
