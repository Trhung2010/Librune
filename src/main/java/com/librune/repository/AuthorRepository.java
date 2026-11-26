package com.librune.repository;

import com.librune.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);
}
