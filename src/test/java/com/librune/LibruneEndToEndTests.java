package com.librune;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.librune.entity.Book;
import com.librune.entity.Role;
import com.librune.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LibruneEndToEndTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void publicHomeAndAdminAuthorizationWork() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Clean Code")));

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "reader1", authorities = {Role.READER})
    void readerCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void adminCanAccessDashboardAndManagementPages() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/books")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/authors")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/readers")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/borrows")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void fullApiBorrowAndAdminApproveReturnSynchronizesInventory() throws Exception {
        // 1. Register new reader via REST API
        String registerPayload = """
                {
                  "username": "reader_e2e",
                  "password": "password123",
                  "fullName": "Trần Thử Nghiệm",
                  "email": "e2e@librune.local",
                  "phone": "0909999888"
                }
                """;
        MvcResult regRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("reader_e2e"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regRes.getResponse().getContentAsString());
        String token = regJson.get("token").asText();

        // 2. Public book catalog endpoints
        mockMvc.perform(get("/api/books/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        mockMvc.perform(get("/api/books").param("keyword", "Clean").param("availableOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));

        // 3. Reader creates a borrow request for 2 copies of Clean Code (id = 1)
        String borrowPayload = """
                {
                  "note": "Mượn học đồ án",
                  "items": [
                    { "bookId": 1, "quantity": 2 }
                  ]
                }
                """;
        MvcResult borrowRes = mockMvc.perform(post("/api/borrows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(borrowPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.details[0].quantity").value(2))
                .andReturn();

        long requestId = objectMapper.readTree(borrowRes.getResponse().getContentAsString()).get("id").asLong();
        Book beforeApprove = bookRepository.findById(1L).orElseThrow();
        assertThat(beforeApprove.getAvailableQuantity()).isEqualTo(10);

        // 4. Admin approves borrow request via Admin Web -> stock decreases from 10 to 8
        mockMvc.perform(post("/admin/borrows/" + requestId + "/approve")
                        .with(csrf())
                        .param("note", "Duyệt giao sách"))
                .andExpect(status().is3xxRedirection());

        Book afterApprove = bookRepository.findById(1L).orElseThrow();
        assertThat(afterApprove.getAvailableQuantity()).isEqualTo(8);

        // 5. Reader sees APPROVED status and notifications
        mockMvc.perform(get("/api/borrows/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)));

        // 6. Admin confirms return -> stock returns from 8 to 10
        mockMvc.perform(post("/admin/borrows/" + requestId + "/return")
                        .with(csrf())
                        .param("note", "Sách nguyên vẹn"))
                .andExpect(status().is3xxRedirection());

        Book afterReturn = bookRepository.findById(1L).orElseThrow();
        assertThat(afterReturn.getAvailableQuantity()).isEqualTo(10);
    }
}
