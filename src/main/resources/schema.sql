-- =========================================================
-- LIBRUNE DATABASE SCHEMA (Canonical source for MySQL & JPA)
-- 9 core tables matching ERD & Entity classes 1-to-1
-- =========================================================

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    biography TEXT
);

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(30) UNIQUE,
    publisher VARCHAR(150),
    publish_year INT,
    total_quantity INT NOT NULL DEFAULT 1,
    available_quantity INT NOT NULL DEFAULT 1,
    description TEXT,
    cover_image VARCHAR(500),
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT chk_books_quantity CHECK (available_quantity >= 0 AND available_quantity <= total_quantity)
);

CREATE TABLE IF NOT EXISTS book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_authors_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS borrow_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    approved_by BIGINT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    request_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    borrow_date DATETIME NULL,
    due_date DATETIME NULL,
    return_date DATETIME NULL,
    note TEXT,
    CONSTRAINT fk_borrow_requests_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_borrow_requests_admin FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS borrow_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_request_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_borrow_details_request FOREIGN KEY (borrow_request_id) REFERENCES borrow_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_details_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT uq_borrow_detail_book UNIQUE (borrow_request_id, book_id),
    CONSTRAINT chk_borrow_detail_qty CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    borrow_request_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_request FOREIGN KEY (borrow_request_id) REFERENCES borrow_requests(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
