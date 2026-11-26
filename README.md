# LIBRUNE – Hệ thống Quản lý Thư viện (Web + Android + Spring Boot + MySQL)

## Nguyên tắc nhất quán của dự án
1. **Chỉ dùng Java, không dùng DTO:**
   - Backend Spring Boot sử dụng trực tiếp JPA `@Entity` cho cả Admin Web (Thymeleaf `.html` render phía server) và REST API JSON (`/api/**` cho Android), kiểm soát serialize bằng `@JsonIgnore`, `@JsonIgnoreProperties`, và `@JsonProperty(access = WRITE_ONLY)`.
   - Android App viết bằng **Android Native Java** (`android/`), model ánh xạ trực tiếp với JSON của Backend.
2. **Một nguồn Schema & Seed Data duy nhất:**
   - [schema.sql](src/main/resources/schema.sql) (9 bảng) và [data.sql](src/main/resources/data.sql) dùng chung cho cả Docker MySQL init và chạy local.
   - `spring.jpa.hibernate.ddl-auto=validate` đảm bảo mọi `@Entity` khớp 100% với `schema.sql`.
3. **Ảnh bìa sách (`cover_image`):** Upload file qua `FileStorageService` vào `${app.upload.dir}/books` (public tại `/uploads/books/...`) và mount volume `./uploads:/app/uploads` trong Docker.

## Tài khoản mẫu (Mật khẩu: `123456`)
- **Trang chính:** `http://localhost:8080/`
- **Admin Web:** `http://localhost:8080/login` — tài khoản `admin` (`ROLE_ADMIN`)
- **Độc giả Android:** tài khoản `reader1` (`ROLE_READER`) hoặc đăng ký trực tiếp trên App.

## Chạy kiểm thử Backend (khuyến nghị JDK 21 hoặc JDK 23)
```powershell
.\mvnw.cmd clean test -B
```

## Chạy bằng Docker (MySQL + Backend)
```powershell
docker compose up --build -d
```

## Build Android App (`android/`)
1. Mở thư mục `android/` bằng **Android Studio**.
2. Nếu chạy trên Emulator, giữ nguyên địa chỉ server mặc định `http://10.0.2.2:8080/`.
3. Nếu cài APK lên điện thoại thật, nhập địa chỉ IP LAN của máy tính chạy Backend (ví dụ `http://192.168.1.10:8080/`) ngay tại màn hình Đăng nhập.
