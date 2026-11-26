package com.librune.service;

import com.librune.entity.Author;
import com.librune.entity.Book;
import com.librune.entity.Category;
import com.librune.repository.BookRepository;
import com.librune.repository.BorrowDetailRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowDetailRepository borrowDetailRepository;
    private final CategoryService categoryService;
    private final AuthorService authorService;
    private final FileStorageService fileStorageService;

    public BookService(
            BookRepository bookRepository,
            BorrowDetailRepository borrowDetailRepository,
            CategoryService categoryService,
            AuthorService authorService,
            FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.borrowDetailRepository = borrowDetailRepository;
        this.categoryService = categoryService;
        this.authorService = authorService;
        this.fileStorageService = fileStorageService;
    }

    public List<Book> findAll() {
        return bookRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Book> findFeatured() {
        return bookRepository.findByFeaturedTrueOrderByCreatedAtDesc();
    }

    public List<Book> findNewest() {
        return bookRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<Book> search(String keyword, Long categoryId, boolean availableOnly) {
        return bookRepository.searchBooks(
                keyword != null ? keyword.trim() : null,
                categoryId,
                availableOnly
        );
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách #" + id));
    }

    @Transactional
    public Book saveBook(Book formBook, Long categoryId, List<Long> authorIds, MultipartFile coverFile) throws IOException {
        Category category = categoryService.findById(categoryId);
        Set<Author> authors = authorService.findAllByIds(authorIds);

        Book target;
        if (formBook.getId() != null) {
            target = findById(formBook.getId());
        } else {
            target = new Book();
        }

        target.setTitle(formBook.getTitle() != null ? formBook.getTitle().trim() : null);
        target.setIsbn(formBook.getIsbn() != null && !formBook.getIsbn().isBlank() ? formBook.getIsbn().trim() : null);
        target.setPublisher(formBook.getPublisher());
        target.setPublishYear(formBook.getPublishYear());
        target.setDescription(formBook.getDescription());
        target.setFeatured(Boolean.TRUE.equals(formBook.getFeatured()));
        target.setCategory(category);
        target.setAuthors(authors);

        int total = formBook.getTotalQuantity() != null ? formBook.getTotalQuantity() : 1;
        int available = formBook.getAvailableQuantity() != null ? formBook.getAvailableQuantity() : total;
        if (total < 0 || available < 0 || available > total) {
            throw new IllegalArgumentException("Số lượng còn phải nằm trong khoảng 0 đến tổng số lượng");
        }
        target.setTotalQuantity(total);
        target.setAvailableQuantity(available);

        if (coverFile != null && !coverFile.isEmpty()) {
            target.setCoverImage(fileStorageService.storeBookCover(coverFile));
        } else if (formBook.getCoverImage() != null && !formBook.getCoverImage().isBlank()) {
            target.setCoverImage(formBook.getCoverImage().trim());
        }

        return bookRepository.save(target);
    }

    @Transactional
    public void deleteById(Long id) {
        if (borrowDetailRepository.existsByBookId(id)) {
            throw new IllegalStateException("Không thể xóa sách đã phát sinh lịch sử mượn");
        }
        bookRepository.deleteById(id);
    }

    public long countBooks() {
        return bookRepository.count();
    }

    public int countAvailableCopies() {
        return bookRepository.findAll().stream()
                .mapToInt(b -> b.getAvailableQuantity() != null ? b.getAvailableQuantity() : 0)
                .sum();
    }
}
