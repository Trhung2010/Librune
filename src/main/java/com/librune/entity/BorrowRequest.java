package com.librune.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reader's borrow request and its approval/return lifecycle.
 */
@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_RETURNED = "RETURNED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(nullable = false, length = 30)
    private String status = STATUS_PENDING;

    @Column(name = "request_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime requestDate;

    @Column(name = "borrow_date", columnDefinition = "DATETIME")
    private LocalDateTime borrowDate;

    @Column(name = "due_date", columnDefinition = "DATETIME")
    private LocalDateTime dueDate;

    @Column(name = "return_date", columnDefinition = "DATETIME")
    private LocalDateTime returnDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "borrowRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BorrowDetail> details = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = STATUS_PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<BorrowDetail> getDetails() {
        return details;
    }

    public void setDetails(List<BorrowDetail> details) {
        this.details = details;
    }
}
