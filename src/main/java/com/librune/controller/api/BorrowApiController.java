package com.librune.controller.api;

import com.librune.entity.BorrowRequest;
import com.librune.entity.User;
import com.librune.service.BorrowService;
import com.librune.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
public class BorrowApiController {

    private final BorrowService borrowService;
    private final UserService userService;

    public BorrowApiController(BorrowService borrowService, UserService userService) {
        this.borrowService = borrowService;
        this.userService = userService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public ResponseEntity<BorrowRequest> createBorrowRequest(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        User reader = userService.findByUsername(authentication.getName());
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        String note = body.get("note") != null ? body.get("note").toString() : null;
        BorrowRequest created = borrowService.createRequest(reader, items, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    public List<BorrowRequest> getMyBorrows(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        User reader = userService.findByUsername(authentication.getName());
        return borrowService.findByUser(reader.getId(), status);
    }

    @GetMapping("/{id}")
    public BorrowRequest getMyBorrowDetail(
            Authentication authentication,
            @PathVariable Long id) {
        User reader = userService.findByUsername(authentication.getName());
        BorrowRequest request = borrowService.findById(id);
        if (!request.getUser().getId().equals(reader.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem phiếu mượn này");
        }
        return request;
    }

    @PutMapping("/{id}/cancel")
    public BorrowRequest cancelBorrowRequest(
            Authentication authentication,
            @PathVariable Long id) {
        User reader = userService.findByUsername(authentication.getName());
        return borrowService.cancelByReader(id, reader.getId());
    }
}
