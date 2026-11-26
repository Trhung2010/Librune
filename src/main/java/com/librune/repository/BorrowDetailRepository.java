package com.librune.repository;

import com.librune.entity.BorrowDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowDetailRepository extends JpaRepository<BorrowDetail, Long> {
    boolean existsByBookId(Long bookId);
}
