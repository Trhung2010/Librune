package com.librune.controller.web;

import com.librune.entity.BorrowRequest;
import com.librune.service.BookService;
import com.librune.service.BorrowService;
import com.librune.service.CategoryService;
import com.librune.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final BorrowService borrowService;

    public AdminDashboardController(
            BookService bookService,
            CategoryService categoryService,
            UserService userService,
            BorrowService borrowService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.borrowService = borrowService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalBooks", bookService.countBooks());
        model.addAttribute("availableCopies", bookService.countAvailableCopies());
        model.addAttribute("totalCategories", categoryService.findAll().size());
        model.addAttribute("totalReaders", userService.countReaders());
        model.addAttribute("totalBorrowRequests", borrowService.countAll());
        model.addAttribute("pendingCount", borrowService.countByStatus(BorrowRequest.STATUS_PENDING));
        model.addAttribute("approvedCount", borrowService.countByStatus(BorrowRequest.STATUS_APPROVED));
        model.addAttribute("returnedCount", borrowService.countByStatus(BorrowRequest.STATUS_RETURNED));
        model.addAttribute("pendingRequests", borrowService.findAll(BorrowRequest.STATUS_PENDING));
        return "admin/dashboard";
    }
}
