package com.librune.controller.web;

import com.librune.entity.User;
import com.librune.service.BorrowService;
import com.librune.service.PaginationSupport;
import com.librune.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;
import java.util.Set;

@Controller
@RequestMapping("/admin/readers")
public class AdminReaderController {

    private static final Set<String> ALLOWED_STATUSES = Set.of(User.STATUS_ACTIVE, User.STATUS_LOCKED);

    private final UserService userService;
    private final BorrowService borrowService;

    public AdminReaderController(UserService userService, BorrowService borrowService) {
        this.userService = userService;
        this.borrowService = borrowService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PaginationSupport.DEFAULT_PAGE_SIZE) int size,
            Model model) {
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            String upper = status.trim().toUpperCase();
            if (ALLOWED_STATUSES.contains(upper)) {
                normalizedStatus = upper;
            } else {
                model.addAttribute("warningMessage", "Trạng thái lọc không hợp lệ, đang hiển thị tất cả độc giả.");
            }
        }

        Page<User> readerPage = userService.searchReaders(keyword, normalizedStatus, page, size);
        model.addAttribute("activePage", "readers");
        model.addAttribute("readerPage", readerPage);
        model.addAttribute("readers", readerPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", normalizedStatus);
        return "admin/readers/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            User reader = userService.findById(id);
            model.addAttribute("activePage", "readers");
            model.addAttribute("reader", reader);
            model.addAttribute("borrowHistory", borrowService.findByUser(reader.getId(), null));
            return "admin/readers/detail";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không tìm thấy độc giả"));
            return "redirect:/admin/readers";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String returnTo,
            Authentication authentication,
            RedirectAttributes ra) {
        try {
            User target = userService.findById(id);
            if (authentication != null && target.getUsername().equalsIgnoreCase(authentication.getName())) {
                ra.addFlashAttribute("errorMessage", "Không thể tự khóa tài khoản của mình");
                return "redirect:/admin/readers" + ("detail".equalsIgnoreCase(returnTo) ? "/" + id : "");
            }
            User updated = userService.toggleStatus(id);
            ra.addFlashAttribute("successMessage",
                    "Đã cập nhật trạng thái tài khoản " + updated.getUsername() + " -> " + updated.getStatus());
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể cập nhật trạng thái"));
        }
        if ("detail".equalsIgnoreCase(returnTo)) {
            return "redirect:/admin/readers/" + id;
        }
        return "redirect:/admin/readers";
    }
}
