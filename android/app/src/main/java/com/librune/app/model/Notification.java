package com.librune.app.model;

public class Notification {
    private Long id;
    private BorrowRequest borrowRequest;
    private String title;
    private String message;
    private Boolean read;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BorrowRequest getBorrowRequest() { return borrowRequest; }
    public void setBorrowRequest(BorrowRequest borrowRequest) { this.borrowRequest = borrowRequest; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
