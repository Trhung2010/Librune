package com.librune.repository;

import com.librune.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            SELECT c FROM Category c
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Category> searchPage(@Param("keyword") String keyword, Pageable pageable);
}
