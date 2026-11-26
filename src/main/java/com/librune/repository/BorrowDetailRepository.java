package com.librune.repository;

import com.librune.entity.BorrowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowDetailRepository extends JpaRepository<BorrowDetail, Long> {
    boolean existsByBookId(Long bookId);

    @Query("""
            SELECT COALESCE(SUM(bd.quantity), 0)
            FROM BorrowDetail bd
            WHERE bd.book.id = :bookId
              AND bd.borrowRequest.status = 'APPROVED'
            """)
    int countActiveLoansByBookId(@Param("bookId") Long bookId);
}
