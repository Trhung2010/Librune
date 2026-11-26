package com.librune.controller.web;

import com.librune.entity.Author;
import com.librune.entity.Book;
import com.librune.service.AuthorService;
import com.librune.service.BookService;
import com.librune.service.CategoryService;
import com.librune.service.PaginationSupport;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private static final int MIN_PUBLISH_YEAR = 1000;
    private static final int MAX_PUBLISH_YEAR = 2100;

    private final BookService bookService;
    private final CategoryService categoryService;
    private final AuthorService authorService;

    public AdminBookController(
            BookService bookService,
            CategoryService categoryService,
            AuthorService authorService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.authorService = authorService;
    }

    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PaginationSupport.DEFAULT_PAGE_SIZE) int size,
            Model model) {
        Page<Book> bookPage = bookService.searchPage(keyword, categoryId, availableOnly, page, size);
        model.addAttribute("activePage", "books");
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("availableOnly", availableOnly);
        return "admin/books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Book book = new Book();
        populateFormModel(model, book, null, Set.of());
        return "admin/books/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Book book = bookService.findById(id);
            Set<Long> selectedAuthorIds = book.getAuthors().stream()
                    .map(Author::getId)
                    .collect(Collectors.toSet());
            Long selectedCategoryId = book.getCategory() != null ? book.getCategory().getId() : null;
            populateFormModel(model, book, selectedCategoryId, selectedAuthorIds);
            return "admin/books/form";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không tìm thấy sách"));
            return "redirect:/admin/books";
        }
    }

    @PostMapping("/save")
    public String saveBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> authorIds,
            @RequestParam(required = false) MultipartFile coverFile,
            Model model,
            RedirectAttributes ra) {
        Set<Long> selectedAuthorIds = authorIds != null ? new HashSet<>(authorIds) : Set.of();

        validateBookForm(book, categoryId, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại các trường thông tin bên dưới.");
            populateFormModel(model, book, categoryId, selectedAuthorIds);
            return "admin/books/form";
        }

        try {
            bookService.saveBook(book, categoryId, authorIds, coverFile);
            ra.addFlashAttribute("successMessage", "Lưu thông tin sách thành công");
            return "redirect:/admin/books";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể lưu thông tin sách"));
            populateFormModel(model, book, categoryId, selectedAuthorIds);
            return "admin/books/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa sách #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể xóa sách"));
        }
        return "redirect:/admin/books";
    }

    private void validateBookForm(Book book, Long categoryId, BindingResult bindingResult) {
        if (categoryId == null) {
            bindingResult.reject("categoryId.required", "Vui lòng chọn danh mục sách");
        }
        if (book.getTitle() != null && book.getTitle().trim().length() > 255) {
            bindingResult.rejectValue("title", "size.title", "Tiêu đề tối đa 255 ký tự");
        }
        if (book.getIsbn() != null && book.getIsbn().trim().length() > 30) {
            bindingResult.rejectValue("isbn", "size.isbn", "Mã ISBN tối đa 30 ký tự");
        }
        if (book.getPublisher() != null && book.getPublisher().trim().length() > 150) {
            bindingResult.rejectValue("publisher", "size.publisher", "Nhà xuất bản tối đa 150 ký tự");
        }
        if (book.getPublishYear() != null
                && (book.getPublishYear() < MIN_PUBLISH_YEAR || book.getPublishYear() > MAX_PUBLISH_YEAR)) {
            bindingResult.rejectValue("publishYear", "range.publishYear",
                    "Năm xuất bản phải từ " + MIN_PUBLISH_YEAR + " đến " + MAX_PUBLISH_YEAR);
        }
        if (book.getTotalQuantity() == null || book.getTotalQuantity() < 0) {
            bindingResult.rejectValue("totalQuantity", "min.totalQuantity", "Tổng số lượng phải lớn hơn hoặc bằng 0");
        }
        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() < 0) {
            bindingResult.rejectValue("availableQuantity", "min.availableQuantity", "Số lượng còn phải lớn hơn hoặc bằng 0");
        }
        if (book.getTotalQuantity() != null && book.getAvailableQuantity() != null
                && book.getAvailableQuantity() > book.getTotalQuantity()) {
            bindingResult.rejectValue("availableQuantity", "max.availableQuantity",
                    "Số lượng còn không được vượt quá tổng số lượng");
        }
        if (book.getCoverImage() != null && !book.getCoverImage().isBlank()) {
            String cover = book.getCoverImage().trim();
            String lower = cover.toLowerCase();
            boolean validCover = lower.startsWith("http://")
                    || lower.startsWith("https://")
                    || cover.startsWith("/uploads/");
            if (!validCover || cover.length() > 255) {
                bindingResult.rejectValue("coverImage", "invalid.coverImage",
                        "URL ảnh bìa phải bắt đầu bằng http://, https:// hoặc /uploads/ (tối đa 255 ký tự)");
            }
        }
    }

    private void populateFormModel(Model model, Book book, Long selectedCategoryId, Set<Long> selectedAuthorIds) {
        model.addAttribute("activePage", "books");
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("selectedAuthorIds", selectedAuthorIds);
    }
}
