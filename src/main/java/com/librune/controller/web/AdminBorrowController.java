package com.librune.controller.web;

import com.librune.entity.User;
import com.librune.service.BorrowService;
import com.librune.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/borrows")
public class AdminBorrowController {

    private final BorrowService borrowService;
    private final UserService userService;

    public AdminBorrowController(BorrowService borrowService, UserService userService) {
        this.borrowService = borrowService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("activePage", "borrows");
        model.addAttribute("borrows", borrowService.findAll(status));
        model.addAttribute("selectedStatus", status);
        return "admin/borrows/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("activePage", "borrows");
            model.addAttribute("borrow", borrowService.findById(id));
            model.addAttribute("defaultDueDate", LocalDate.now().plusDays(14));
            return "admin/borrows/detail";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/borrows";
        }
    }

    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) String note,
            Authentication authentication,
            RedirectAttributes ra) {
        try {
            User admin = userService.findByUsername(authentication.getName());
            LocalDateTime dueDateTime = dueDate != null ? dueDate.atTime(23, 59, 59) : null;
            borrowService.approveRequest(id, admin, dueDateTime, note);
            ra.addFlashAttribute("successMessage", "Đã duyệt phiếu mượn #" + id + " và cập nhật tồn kho");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/borrows/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            Authentication authentication,
            RedirectAttributes ra) {
        try {
            User admin = userService.findByUsername(authentication.getName());
            borrowService.rejectRequest(id, admin, note);
            ra.addFlashAttribute("successMessage", "Đã từ chối phiếu mượn #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/borrows/" + id;
    }

    @PostMapping("/{id}/return")
    public String confirmReturn(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            Authentication authentication,
            RedirectAttributes ra) {
        try {
            User admin = userService.findByUsername(authentication.getName());
            borrowService.confirmReturn(id, admin, note);
            ra.addFlashAttribute("successMessage", "Đã xác nhận trả sách phiếu #" + id + " và hoàn kho");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/borrows/" + id;
    }
}
