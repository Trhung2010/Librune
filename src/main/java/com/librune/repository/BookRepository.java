package com.librune.repository;

import com.librune.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b WHERE b.availableQuantity > 0")
    int countAvailableCopies();

    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableQuantity > 0")
    long countAvailableTitles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithPessimisticLock(@Param("id") Long id);

    List<Book> findByFeaturedTrueOrderByCreatedAtDesc();

    List<Book> findTop10ByOrderByCreatedAtDesc();

    List<Book> findTop5ByAvailableQuantityLessThanEqualOrderByAvailableQuantityAsc(int threshold);

    @Query("""
            SELECT DISTINCT b FROM Book b
            LEFT JOIN b.authors a
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:availableOnly = false OR b.availableQuantity > 0)
            ORDER BY b.createdAt DESC
            """)
    List<Book> searchBooks(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("availableOnly") boolean availableOnly
    );

    @Query(
            value = """
                    SELECT DISTINCT b FROM Book b
                    LEFT JOIN b.authors a
                    WHERE (:keyword IS NULL OR :keyword = ''
                           OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:categoryId IS NULL OR b.category.id = :categoryId)
                      AND (:availableOnly = false OR b.availableQuantity > 0)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT b.id) FROM Book b
                    LEFT JOIN b.authors a
                    WHERE (:keyword IS NULL OR :keyword = ''
                           OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:categoryId IS NULL OR b.category.id = :categoryId)
                      AND (:availableOnly = false OR b.availableQuantity > 0)
                    """
    )
    Page<Book> searchBooksPage(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("availableOnly") boolean availableOnly,
            Pageable pageable
    );

    boolean existsByCategoryId(Long categoryId);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);
}
