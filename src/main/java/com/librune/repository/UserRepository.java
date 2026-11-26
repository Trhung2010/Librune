package com.librune.repository;

import com.librune.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRoleNameOrderByCreatedAtDesc(String roleName);
    long countByRoleName(String roleName);

    @Query("""
            SELECT u FROM User u
            WHERE u.role.name = :roleName
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR :status = '' OR UPPER(u.status) = UPPER(:status))
            """)
    Page<User> searchByRole(
            @Param("roleName") String roleName,
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );
}
