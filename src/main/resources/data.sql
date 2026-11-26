-- =========================================================
-- LIBRUNE SEED DATA (Idempotent - safe for Docker & local)
-- Default password for seeded accounts: 123456
-- =========================================================

INSERT IGNORE INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Quản trị viên / Thủ thư'),
(2, 'ROLE_READER', 'Độc giả sử dụng ứng dụng Android');

INSERT IGNORE INTO users (id, role_id, username, password, full_name, email, phone, address, status) VALUES
(1, 1, 'admin', '$2a$10$VdSZi2.xSy0wd4LhBCJyQOoN2B4.0CM9PjMBt7hnLNEpJDSQ1ZJti', 'Quản trị Thư viện', 'admin@librune.local', '0901000001', 'Thư viện Trung tâm', 'ACTIVE'),
(2, 2, 'reader1', '$2a$10$VdSZi2.xSy0wd4LhBCJyQOoN2B4.0CM9PjMBt7hnLNEpJDSQ1ZJti', 'Nguyễn Văn An', 'an.nguyen@librune.local', '0902000002', 'Hà Nội', 'ACTIVE');

INSERT IGNORE INTO categories (id, name, description) VALUES
(1, 'Công nghệ thông tin', 'Sách lập trình, kiến trúc phần mềm và cơ sở dữ liệu'),
(2, 'Kỹ năng sống', 'Sách phát triển bản thân và phương pháp làm việc'),
(3, 'Văn học', 'Tiểu thuyết, truyện ngắn trong và ngoài nước');

INSERT IGNORE INTO authors (id, name, biography) VALUES
(1, 'Robert C. Martin', 'Kỹ sư phần mềm và tác giả của Clean Code'),
(2, 'Joshua Bloch', 'Tác giả sách Effective Java'),
(3, 'Nguyễn Nhật Ánh', 'Nhà văn Việt Nam nổi tiếng với tác phẩm tuổi học trò');

INSERT IGNORE INTO books (id, category_id, title, isbn, publisher, publish_year, total_quantity, available_quantity, description, cover_image, is_featured) VALUES
(1, 1, 'Clean Code', '9780132350884', 'Prentice Hall', 2008, 10, 10, 'Cẩm nang viết mã nguồn sạch dành cho lập trình viên.', NULL, TRUE),
(2, 1, 'Effective Java', '9780134685991', 'Addison-Wesley', 2018, 8, 8, 'Các nguyên tắc thực hành tốt nhất khi lập trình Java.', NULL, TRUE),
(3, 3, 'Mắt Biếc', '9786042095617', 'NXB Trẻ', 2019, 12, 12, 'Tác phẩm văn học nổi tiếng của nhà văn Nguyễn Nhật Ánh.', NULL, FALSE);

INSERT IGNORE INTO book_authors (book_id, author_id) VALUES
(1, 1),
(2, 2),
(3, 3);
