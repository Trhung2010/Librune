package com.librune.repository;

import com.librune.entity.BorrowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByUserIdOrderByRequestDateDesc(Long userId);
    List<BorrowRequest> findByStatusOrderByRequestDateDesc(String status);
    List<BorrowRequest> findAllByOrderByRequestDateDesc();
    List<BorrowRequest> findTop8ByOrderByRequestDateDesc();
    long countByStatus(String status);

    @Query("""
            SELECT br FROM BorrowRequest br
            WHERE (:status IS NULL OR :status = '' OR UPPER(br.status) = UPPER(:status))
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(br.user.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(br.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR CAST(br.id AS string) LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<BorrowRequest> searchBorrows(
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
