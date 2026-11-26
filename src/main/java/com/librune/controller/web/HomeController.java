package com.librune.controller.web;

import com.librune.service.BookService;
import com.librune.service.CategoryService;
import com.librune.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final UserService userService;

    public HomeController(BookService bookService, CategoryService categoryService, UserService userService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        boolean hasFilter = (keyword != null && !keyword.isBlank()) || categoryId != null;
        model.addAttribute("featuredBooks", bookService.findFeatured());
        model.addAttribute("books", hasFilter
                ? bookService.search(keyword, categoryId, false)
                : bookService.findNewest());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("totalBooks", bookService.countBooks());
        model.addAttribute("availableCopies", bookService.countAvailableCopies());
        model.addAttribute("totalReaders", userService.countReaders());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
