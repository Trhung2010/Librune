package com.librune.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.librune.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pure Java HMAC-SHA256 JWT token service (no external JWT library required).
 */
@Service
public class JwtService {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final long MAX_EXPIRATION_SECONDS = 86400L;
    private final String secret;
    private final byte[] secretBytes;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${app.jwt.secret:${JWT_SECRET:}}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds,
            ObjectMapper objectMapper) {
        this.secret = secret;
        this.secretBytes = secret != null ? secret.getBytes(StandardCharsets.UTF_8) : new byte[0];
        this.expirationSeconds = expirationSeconds;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void validateConfig() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT secret (app.jwt.secret / JWT_SECRET) must not be blank. " +
                "Set the JWT_SECRET environment variable to a string of at least 32 characters.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret is too short (" + secret.length() + " chars). " +
                "It must be at least 32 characters long to meet minimum security requirements.");
        }
        if (expirationSeconds <= 0 || expirationSeconds > MAX_EXPIRATION_SECONDS) {
            throw new IllegalStateException(
                "JWT expirationSeconds (" + expirationSeconds + ") must be between 1 and " +
                MAX_EXPIRATION_SECONDS + " seconds (1 day).");
        }
    }

    public String generateToken(User user) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getUsername());
            payload.put("uid", user.getId());
            payload.put("role", user.getRole().getName());
            payload.put("iat", now);
            payload.put("exp", now + expirationSeconds);

            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = base64UrlEncode(hmacSha256(signingInput));
            return signingInput + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo token đăng nhập", ex);
        }
    }

    public String extractUsernameIfValid(String token) {
        Map<String, Object> claims = parseValidClaims(token);
        if (claims == null) {
            return null;
        }
        Object sub = claims.get("sub");
        return sub != null ? sub.toString() : null;
    }

    public Instant extractExpirationIfValid(String token) {
        Map<String, Object> claims = parseValidClaims(token);
        if (claims == null) {
            return null;
        }
        Object expObj = claims.get("exp");
        return expObj instanceof Number expNum ? Instant.ofEpochSecond(expNum.longValue()) : null;
    }

    private Map<String, Object> parseValidClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSig = hmacSha256(signingInput);
            byte[] actualSig = Base64.getUrlDecoder().decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSig, actualSig)) {
                return null;
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = objectMapper.readValue(payloadBytes, new TypeReference<>() {});
            Object expObj = claims.get("exp");
            if (!(expObj instanceof Number expNum)) {
                return null;
            }
            if (Instant.now().getEpochSecond() > expNum.longValue()) {
                return null;
            }
            return claims;
        } catch (Exception ex) {
            return null;
        }
    }

    private byte[] hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALG);
        mac.init(new SecretKeySpec(secretBytes, HMAC_ALG));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
