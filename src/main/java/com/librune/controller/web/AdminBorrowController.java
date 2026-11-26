package com.librune.controller.web;

import com.librune.entity.BorrowRequest;
import com.librune.entity.User;
import com.librune.service.BorrowService;
import com.librune.service.PaginationSupport;
import com.librune.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Controller
@RequestMapping("/admin/borrows")
public class AdminBorrowController {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            BorrowRequest.STATUS_PENDING,
            BorrowRequest.STATUS_APPROVED,
            BorrowRequest.STATUS_RETURNED,
            BorrowRequest.STATUS_REJECTED,
            BorrowRequest.STATUS_CANCELLED
    );

    private final BorrowService borrowService;
    private final UserService userService;

    public AdminBorrowController(BorrowService borrowService, UserService userService) {
        this.borrowService = borrowService;
        this.userService = userService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PaginationSupport.DEFAULT_PAGE_SIZE) int size,
            Model model) {
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            String upper = status.trim().toUpperCase();
            if (ALLOWED_STATUSES.contains(upper)) {
                normalizedStatus = upper;
            } else {
                model.addAttribute("warningMessage", "Trạng thái phiếu mượn không hợp lệ, đang hiển thị tất cả.");
            }
        }

        Page<BorrowRequest> borrowPage = borrowService.searchPage(normalizedStatus, keyword, page, size);
        model.addAttribute("activePage", "borrows");
        model.addAttribute("borrowPage", borrowPage);
        model.addAttribute("borrows", borrowPage.getContent());
        model.addAttribute("selectedStatus", normalizedStatus);
        model.addAttribute("keyword", keyword);
        model.addAttribute("allCount", borrowService.countAll());
        model.addAttribute("pendingCount", borrowService.countByStatus(BorrowRequest.STATUS_PENDING));
        model.addAttribute("approvedCount", borrowService.countByStatus(BorrowRequest.STATUS_APPROVED));
        model.addAttribute("returnedCount", borrowService.countByStatus(BorrowRequest.STATUS_RETURNED));
        model.addAttribute("rejectedCount", borrowService.countByStatus(BorrowRequest.STATUS_REJECTED));
        model.addAttribute("cancelledCount", borrowService.countByStatus(BorrowRequest.STATUS_CANCELLED));
        return "admin/borrows/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("activePage", "borrows");
            model.addAttribute("borrow", borrowService.findById(id));
            model.addAttribute("defaultDueDate", LocalDate.now().plusDays(14));
            model.addAttribute("minDueDate", LocalDate.now());
            return "admin/borrows/detail";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không tìm thấy phiếu mượn"));
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
            if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Hạn trả dự kiến không được nhỏ hơn ngày hiện tại");
            }
            User admin = userService.findByUsername(authentication.getName());
            LocalDateTime dueDateTime = dueDate != null ? dueDate.atTime(23, 59, 59) : null;
            borrowService.approveRequest(id, admin, dueDateTime, note);
            ra.addFlashAttribute("successMessage", "Đã duyệt phiếu mượn #" + id + " và cập nhật tồn kho");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể duyệt phiếu mượn"));
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
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể từ chối phiếu mượn"));
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
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể xác nhận trả sách"));
        }
        return "redirect:/admin/borrows/" + id;
    }
}
