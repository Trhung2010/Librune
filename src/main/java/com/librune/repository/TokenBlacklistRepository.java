package com.librune.repository;

import com.librune.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByTokenHash(String tokenHash);

    long deleteByExpiresAtBefore(LocalDateTime now);
}
