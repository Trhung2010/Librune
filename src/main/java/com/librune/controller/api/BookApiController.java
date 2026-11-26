package com.librune.controller.api;

import com.librune.entity.Book;
import com.librune.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private final BookService bookService;

    public BookApiController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean availableOnly) {
        return bookService.search(keyword, categoryId, availableOnly);
    }

    @GetMapping("/featured")
    public List<Book> getFeaturedBooks() {
        return bookService.findFeatured();
    }

    @GetMapping("/newest")
    public List<Book> getNewestBooks() {
        return bookService.findNewest();
    }

    @GetMapping("/{id}")
    public Book getBookDetail(@PathVariable Long id) {
        return bookService.findById(id);
    }
}
