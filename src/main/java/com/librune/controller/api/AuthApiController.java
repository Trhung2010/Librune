package com.librune.controller.api;

import com.librune.entity.User;
import com.librune.service.JwtService;
import com.librune.service.TokenBlacklistService;
import com.librune.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthApiController(
            UserService userService,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User input) {
        User created = userService.registerReader(input);
        String token = jwtService.generateToken(created);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        User user = userService.authenticate(username, password);
        String token = jwtService.generateToken(user);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            Instant expiresAt = jwtService.extractExpirationIfValid(token);
            if (expiresAt != null) {
                tokenBlacklistService.blacklistToken(token, expiresAt);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }
}
