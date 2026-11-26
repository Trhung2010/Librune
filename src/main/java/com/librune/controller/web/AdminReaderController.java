package com.librune.controller.web;

import com.librune.entity.User;
import com.librune.service.BorrowService;
import com.librune.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/readers")
public class AdminReaderController {

    private final UserService userService;
    private final BorrowService borrowService;

    public AdminReaderController(UserService userService, BorrowService borrowService) {
        this.userService = userService;
        this.borrowService = borrowService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activePage", "readers");
        model.addAttribute("readers", userService.findAllReaders());
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
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/readers";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            User updated = userService.toggleStatus(id);
            ra.addFlashAttribute("successMessage",
                    "Đã cập nhật trạng thái tài khoản " + updated.getUsername() + " -> " + updated.getStatus());
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/readers";
    }
}
