package com.librune.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

/**
 * Represents a single book line item inside a borrow request.
 */
@Entity
@Table(
        name = "borrow_details",
        uniqueConstraints = @UniqueConstraint(name = "uq_borrow_detail_book", columnNames = {"borrow_request_id", "book_id"})
)
public class BorrowDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrow_request_id", nullable = false)
    private BorrowRequest borrowRequest;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Min(1)
    @Column(nullable = false)
    private Integer quantity = 1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BorrowRequest getBorrowRequest() {
        return borrowRequest;
    }

    public void setBorrowRequest(BorrowRequest borrowRequest) {
        this.borrowRequest = borrowRequest;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
