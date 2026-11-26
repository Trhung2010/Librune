package com.librune.repository;

import com.librune.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByFeaturedTrueOrderByCreatedAtDesc();

    List<Book> findTop10ByOrderByCreatedAtDesc();

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

    boolean existsByCategoryId(Long categoryId);
}
