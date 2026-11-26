package com.librune.service;

import com.librune.entity.TokenBlacklist;
import com.librune.repository.TokenBlacklistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

/**
 * Manages revoked JWT token hashes so logged-out tokens cannot be reused.
 */
@Service
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Transactional
    public void blacklistToken(String token, Instant expiresAt) {
        if (token == null || token.isBlank() || expiresAt == null) {
            return;
        }
        String tokenHash = sha256Hex(token);
        if (!tokenBlacklistRepository.existsByTokenHash(tokenHash)) {
            TokenBlacklist entry = new TokenBlacklist();
            entry.setTokenHash(tokenHash);
            entry.setExpiresAt(LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
            tokenBlacklistRepository.save(entry);
        }
        tokenBlacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now(ZoneOffset.UTC));
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return tokenBlacklistRepository.existsByTokenHash(sha256Hex(token));
    }

    private String sha256Hex(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }
}
