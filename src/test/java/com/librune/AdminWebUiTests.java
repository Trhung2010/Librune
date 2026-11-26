package com.librune;

import com.librune.entity.Book;
import com.librune.entity.Category;
import com.librune.entity.Role;
import com.librune.repository.BookRepository;
import com.librune.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminWebUiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void publicHomeAndLoginPagesRenderCleanly() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thư viện Librune")))
                .andExpect(content().string(containsString("Clean Code")))
                .andExpect(content().string(containsString("Mắt Biếc")))
                .andExpect(content().string(not(containsString("th:text="))));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LIBRUNE ADMIN")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void allAdminPagesAndFormsRenderWithoutTemplateErrors() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bảng điều khiển")))
                .andExpect(content().string(containsString("borrowStatusChart")));

        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bookPage", "books"))
                .andExpect(content().string(containsString("Quản lý Sách")));

        mockMvc.perform(get("/admin/books/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thông tin sách mới")));

        mockMvc.perform(get("/admin/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Clean Code")));

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryPage", "categories"))
                .andExpect(content().string(containsString("Công nghệ thông tin")));

        mockMvc.perform(get("/admin/categories/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thêm mới")))
                .andExpect(content().string(not(containsString("${category.id"))));

        mockMvc.perform(get("/admin/categories/1/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cập nhật")));

        mockMvc.perform(get("/admin/authors"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("authorPage", "authors"))
                .andExpect(content().string(containsString("Robert C. Martin")));

        mockMvc.perform(get("/admin/authors/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thông tin tác giả mới")));

        mockMvc.perform(get("/admin/authors/1/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Robert C. Martin")));

        mockMvc.perform(get("/admin/readers"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("readerPage", "readers"))
                .andExpect(content().string(containsString("reader1")));

        mockMvc.perform(get("/admin/readers/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nguyễn Văn An")));

        mockMvc.perform(get("/admin/borrows"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("borrowPage", "borrows"));
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void paginationAndFilteringWorkAcrossLists() throws Exception {
        Category cat = categoryRepository.findById(1L).orElseThrow();
        for (int i = 1; i <= 7; i++) {
            Book b = new Book();
            b.setCategory(cat);
            b.setTitle("Paged Book " + i);
            b.setTotalQuantity(3);
            b.setAvailableQuantity(3);
            bookRepository.save(b);
        }

        var page0Res = mockMvc.perform(get("/admin/books").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bookPage"))
                .andExpect(content().string(containsString("Hiển thị")))
                .andReturn();
        Page<?> page0 = (Page<?>) page0Res.getModelAndView().getModel().get("bookPage");
        assertThat(page0.getSize()).isEqualTo(5);
        assertThat(page0.getTotalElements()).isGreaterThanOrEqualTo(10);
        assertThat(page0.getTotalPages()).isGreaterThanOrEqualTo(2);

        mockMvc.perform(get("/admin/books").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trang")));

        mockMvc.perform(get("/admin/categories").param("keyword", "Văn học"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Văn học")))
                .andExpect(content().string(not(containsString("Công nghệ thông tin"))));

        mockMvc.perform(get("/admin/authors").param("keyword", "Robert"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Robert C. Martin")))
                .andExpect(content().string(not(containsString("Joshua Bloch"))));

        mockMvc.perform(get("/admin/readers").param("keyword", "reader1").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("reader1")));

        mockMvc.perform(get("/admin/readers").param("status", "INVALID_STATUS"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("warningMessage"));

        mockMvc.perform(get("/admin/borrows").param("status", "UNKNOWN_STATUS"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("warningMessage"));
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void bookFormValidationRejectsInvalidInputsAndSavesValidBook() throws Exception {
        // Blank title + invalid quantities + unsafe cover URL
        mockMvc.perform(post("/admin/books/save")
                        .with(csrf())
                        .param("title", "   ")
                        .param("categoryId", "1")
                        .param("totalQuantity", "2")
                        .param("availableQuantity", "5")
                        .param("publishYear", "999")
                        .param("coverImage", "file:///etc/passwd"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/books/form"))
                .andExpect(model().attributeHasFieldErrors("book", "title", "availableQuantity", "publishYear", "coverImage"))
                .andExpect(model().attributeExists("errorMessage"));

        // Valid book saves and redirects
        mockMvc.perform(post("/admin/books/save")
                        .with(csrf())
                        .param("title", "Domain-Driven Design")
                        .param("isbn", "9780321125217")
                        .param("categoryId", "1")
                        .param("authorIds", "1")
                        .param("publisher", "Addison-Wesley")
                        .param("publishYear", "2003")
                        .param("totalQuantity", "5")
                        .param("availableQuantity", "4")
                        .param("coverImage", "https://example.com/ddd.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void categoryAndAuthorValidationWorkProperly() throws Exception {
        mockMvc.perform(post("/admin/categories/save")
                        .with(csrf())
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/form"))
                .andExpect(model().attributeHasFieldErrors("category", "name"));

        // Duplicate name check on update (id=2 trying to take name of id=1)
        mockMvc.perform(post("/admin/categories/save")
                        .with(csrf())
                        .param("id", "2")
                        .param("name", "Công nghệ thông tin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/form"))
                .andExpect(model().attributeExists("errorMessage"));

        mockMvc.perform(post("/admin/authors/save")
                        .with(csrf())
                        .param("name", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/authors/form"))
                .andExpect(model().attributeHasFieldErrors("author", "name"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {Role.ADMIN})
    void adminSelfLockAndPastDueDateAreRejected() throws Exception {
        mockMvc.perform(post("/admin/readers/1/toggle-status").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/readers"))
                .andExpect(flash().attributeExists("errorMessage"));

        mockMvc.perform(post("/admin/borrows/1/approve")
                        .with(csrf())
                        .param("dueDate", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/borrows/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
