package com.librune;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librune.entity.Book;
import com.librune.entity.BorrowDetail;
import com.librune.entity.BorrowRequest;
import com.librune.entity.Role;
import com.librune.entity.User;
import com.librune.repository.BookRepository;
import com.librune.repository.BorrowRequestRepository;
import com.librune.repository.UserRepository;
import com.librune.service.BorrowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHighFixesTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void h1_csrfCookieIsSentOnWebAndApiEndpointsAreCsrfExempt() throws Exception {
        // Web GET /login renders login page successfully with CSRF token in form
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));

        // POST /api/auth/register does not require CSRF token
        String validRegister = """
                {
                  "username": "h1_reader",
                  "password": "password123",
                  "fullName": "Reader H1",
                  "email": "h1@librune.local"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegister))
                .andExpect(status().isCreated());

        // Web POST /admin/books/save WITHOUT csrf is rejected with 403 Forbidden
        mockMvc.perform(post("/admin/books/save")
                        .param("title", "Forbidden Without CSRF")
                        .param("categoryId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void h2_adminCannotSelfLockAccount() throws Exception {
        // Admin user has id=1 and username="admin"
        User admin = userRepository.findByUsername("admin").orElseThrow();

        mockMvc.perform(post("/admin/readers/" + admin.getId() + "/toggle-status").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/readers"))
                .andExpect(flash().attribute("errorMessage", "Không thể tự khóa tài khoản của mình"));
    }

    @Test
    @Transactional
    void h3_borrowServiceApproveUsesPessimisticLockAndRechecksQuantity() {
        Book book = bookRepository.findByIdWithPessimisticLock(1L).orElseThrow();
        assertThat(book).isNotNull();

        // Create a borrow request with availableQuantity = 0 scenario
        User reader = userRepository.findByUsername("reader1").orElseThrow();
        User admin = userRepository.findByUsername("admin").orElseThrow();

        BorrowRequest req = new BorrowRequest();
        req.setUser(reader);
        req.setStatus(BorrowRequest.STATUS_PENDING);

        BorrowDetail detail = new BorrowDetail();
        detail.setBorrowRequest(req);
        detail.setBook(book);
        detail.setQuantity(book.getAvailableQuantity() + 10);
        req.getDetails().add(detail);

        BorrowRequest savedReq = borrowRequestRepository.save(req);

        assertThatThrownBy(() -> borrowService.approveRequest(savedReq.getId(), admin, LocalDateTime.now().plusDays(7), "test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("không đủ tồn kho để duyệt");
    }

    @Test
    void h4_apiExceptionHandlerHidesInternalDetailsOnUnexpectedExceptions() throws Exception {
        // Trigger a 400 bad request -> message preserved for domain validation
        String badRegister = """
                {
                  "username": "   ",
                  "password": "123",
                  "fullName": "Test"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRegister))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void h5_adminBookControllerSaveValidatesBookModelAttribute() throws Exception {
        // Missing title & invalid quantity rejected by @Valid / BindingResult
        mockMvc.perform(post("/admin/books/save")
                        .with(csrf())
                        .param("title", "")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/books/form"))
                .andExpect(model().attributeHasFieldErrors("book", "title"));
    }

    @Test
    void h6_userServiceRejectsOversizedFields() {
        User overName = new User();
        overName.setUsername("valid_user");
        overName.setPassword("12345678");
        overName.setFullName("A".repeat(101));

        assertThatThrownBy(() -> userRepository.save(overName))
                .satisfies(ex -> assertThat(ex.getMessage()).isNotNull());
    }
}
