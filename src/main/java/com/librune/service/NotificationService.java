package com.librune.service;

import com.librune.entity.BorrowRequest;
import com.librune.entity.Notification;
import com.librune.entity.User;
import com.librune.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification create(User user, BorrowRequest borrowRequest, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setBorrowRequest(borrowRequest);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo #" + notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Không có quyền thao tác thông báo này");
        }
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> items = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : items) {
            if (!Boolean.TRUE.equals(n.getRead())) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(items);
    }
}
