package com.librune.controller.api;

import com.librune.entity.Notification;
import com.librune.entity.User;
import com.librune.service.NotificationService;
import com.librune.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationApiController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public List<Notification> getMyNotifications(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return notificationService.findByUserId(user.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return Map.of("count", notificationService.countUnread(user.getId()));
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(Authentication authentication, @PathVariable Long id) {
        User user = userService.findByUsername(authentication.getName());
        return notificationService.markAsRead(id, user.getId());
    }

    @PutMapping("/read-all")
    public Map<String, String> markAllAsRead(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        notificationService.markAllAsRead(user.getId());
        return Map.of("message", "Đã đánh dấu tất cả thông báo là đã đọc");
    }
}
