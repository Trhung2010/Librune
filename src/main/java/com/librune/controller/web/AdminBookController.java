package com.librune.controller.web;

import com.librune.entity.Author;
import com.librune.entity.Book;
import com.librune.service.AuthorService;
import com.librune.service.BookService;
import com.librune.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

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
            Model model) {
        model.addAttribute("activePage", "books");
        model.addAttribute("books", bookService.search(keyword, categoryId, availableOnly));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("availableOnly", availableOnly);
        return "admin/books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Book book = new Book();
        populateFormModel(model, book, Set.of());
        return "admin/books/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Book book = bookService.findById(id);
            Set<Long> selectedAuthorIds = book.getAuthors().stream()
                    .map(Author::getId)
                    .collect(Collectors.toSet());
            populateFormModel(model, book, selectedAuthorIds);
            return "admin/books/form";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/books";
        }
    }

    @PostMapping("/save")
    public String saveBook(
            @ModelAttribute Book book,
            @RequestParam Long categoryId,
            @RequestParam(required = false) List<Long> authorIds,
            @RequestParam(required = false) MultipartFile coverFile,
            RedirectAttributes ra) {
        try {
            bookService.saveBook(book, categoryId, authorIds, coverFile);
            ra.addFlashAttribute("successMessage", "Lưu thông tin sách thành công");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa sách #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/books";
    }

    private void populateFormModel(Model model, Book book, Set<Long> selectedAuthorIds) {
        model.addAttribute("activePage", "books");
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("selectedAuthorIds", selectedAuthorIds);
    }
}
