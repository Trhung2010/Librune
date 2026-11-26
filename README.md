# LIBRUNE – Hệ thống Quản lý Thư viện (Spring Boot + Thymeleaf + Android Java + MySQL)

## Nguyên tắc nhất quán của dự án
1. **Chỉ dùng Java, không dùng DTO:** Sử dụng trực tiếp JPA `@Entity` cho cả Thymeleaf Admin Web và REST API JSON (`/api/**`), kiểm soát serialize bằng `@JsonIgnore`, `@JsonIgnoreProperties`, và `@JsonProperty(access = WRITE_ONLY)`.
2. **Một nguồn Schema & Seed Data duy nhất:**
   - [schema.sql](src/main/resources/schema.sql) và [data.sql](src/main/resources/data.sql) là nguồn chuẩn duy nhất cho cả Docker MySQL init lẫn cấu hình local.
   - `spring.jpa.hibernate.ddl-auto=validate` đảm bảo toàn bộ 8 `@Entity` (ánh xạ 9 bảng) khớp 100% với `schema.sql`.
3. **Ảnh bìa sách (`cover_image`):** Upload file qua `FileStorageService` vào `${app.upload.dir}/books` (public tại `/uploads/books/...`) và mount volume `./uploads:/app/uploads` trong Docker để không mất ảnh khi khởi động lại container.

## Tài khoản mẫu (Mật khẩu: `123456`)
- Admin Web: `admin` (`ROLE_ADMIN`)
- Độc giả Android: `reader1` (`ROLE_READER`)

## Chạy kiểm thử (không cần cài sẵn Maven/MySQL)
```powershell
.\mvnw.cmd test
```

## Chạy bằng Docker (MySQL + Backend)
```powershell
docker compose up --build -d
```
- Admin Web / Backend: `http://localhost:8080`
- MySQL: `localhost:3306` (database `librune`, user `librune`, password `librune_password`)
