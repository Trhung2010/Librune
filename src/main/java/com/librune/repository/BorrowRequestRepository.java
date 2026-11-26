package com.librune.repository;

import com.librune.entity.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByUserIdOrderByRequestDateDesc(Long userId);
    List<BorrowRequest> findByStatusOrderByRequestDateDesc(String status);
    List<BorrowRequest> findAllByOrderByRequestDateDesc();
    long countByStatus(String status);
}
