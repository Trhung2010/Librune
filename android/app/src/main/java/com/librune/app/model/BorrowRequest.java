package com.librune.app.model;

import java.util.List;

public class BorrowRequest {
    private Long id;
    private User user;
    private User approvedBy;
    private String status;
    private String requestDate;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private String note;
    private List<BorrowDetail> details;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }
    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<BorrowDetail> getDetails() { return details; }
    public void setDetails(List<BorrowDetail> details) { this.details = details; }
}
