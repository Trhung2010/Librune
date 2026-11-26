package com.librune;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librune.repository.TokenBlacklistRepository;
import com.librune.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityCriticalFixesTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Test
    void c1_jwtServiceRejectsBlankOrShortSecretAndExcessiveExpiry() {
        JwtService blankSecret = new JwtService("   ", 86400, objectMapper);
        assertThatThrownBy(blankSecret::validateConfig)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blank");

        JwtService shortSecret = new JwtService("too-short-secret-1234567890", 86400, objectMapper);
        assertThatThrownBy(shortSecret::validateConfig)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");

        JwtService excessiveExpiry = new JwtService("valid-secret-key-at-least-32-chars-long", 86401, objectMapper);
        assertThatThrownBy(excessiveExpiry::validateConfig)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("86400");

        JwtService valid = new JwtService("valid-secret-key-at-least-32-chars-long", 86400, objectMapper);
        valid.validateConfig();
    }

    @Test
    void c2_logoutBlacklistsTokenAndRejectsSubsequentRequests() throws Exception {
        String loginPayload = """
                {
                  "username": "reader1",
                  "password": "123456"
                }
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        mockMvc.perform(get("/api/borrows/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        long beforeCount = tokenBlacklistRepository.count();

        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(tokenBlacklistRepository.count()).isEqualTo(beforeCount + 1);

        mockMvc.perform(get("/api/borrows/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void c3_corsAllowsConfiguredOriginAndRejectsWildcardOrigin() throws Exception {
        mockMvc.perform(options("/api/books")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8080"));

        mockMvc.perform(options("/api/books")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void c4_createBorrowRequestRejectsNonListItemsWith400() throws Exception {
        String loginPayload = """
                {
                  "username": "reader1",
                  "password": "123456"
                }
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        String badPayload = """
                {
                  "items": "invalid-string-instead-of-list",
                  "note": "test"
                }
                """;
        mockMvc.perform(post("/api/borrows")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request format"));
    }
}
