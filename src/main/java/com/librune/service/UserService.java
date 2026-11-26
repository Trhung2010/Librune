package com.librune.service;

import com.librune.entity.Role;
import com.librune.entity.User;
import com.librune.repository.RoleRepository;
import com.librune.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private static final String DUMMY_BCRYPT_HASH = "$2a$10$w0uK/b4b5E4hWJ6g5s3FheD6qF9KkWlRjCq9eQz5Mh9p.g4O8fF7C";
    private static final Set<String> ALLOWED_STATUSES = Set.of(User.STATUS_ACTIVE, User.STATUS_LOCKED);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerReader(User input) {
        if (input.getUsername() == null || input.getUsername().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (input.getPassword() == null || input.getPassword().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải từ 8 ký tự trở lên");
        }
        if (input.getFullName() == null || input.getFullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (input.getFullName().trim().length() > 100) {
            throw new IllegalArgumentException("Họ tên không được vượt quá 100 ký tự");
        }
        if (input.getPhone() != null && input.getPhone().trim().length() > 100) {
            throw new IllegalArgumentException("Số điện thoại không được vượt quá 100 ký tự");
        }
        if (input.getAddress() != null && input.getAddress().trim().length() > 100) {
            throw new IllegalArgumentException("Địa chỉ không được vượt quá 100 ký tự");
        }

        String username = input.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        String email = input.getEmail() != null && !input.getEmail().isBlank() ? input.getEmail().trim() : null;
        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role readerRole = roleRepository.findByName(Role.READER)
                .orElseThrow(() -> new IllegalStateException("Chưa khởi tạo role ROLE_READER"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setFullName(input.getFullName().trim());
        user.setEmail(email);
        user.setPhone(input.getPhone() != null ? input.getPhone().trim() : null);
        user.setAddress(input.getAddress() != null ? input.getAddress().trim() : null);
        user.setRole(readerRole);
        user.setStatus(User.STATUS_ACTIVE);
        return userRepository.save(user);
    }

    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            passwordEncoder.matches(rawPassword, DUMMY_BCRYPT_HASH);
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu");
        }
        User user = userOpt.get();
        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPassword());
        if (!passwordMatches) {
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu");
        }
        if (!User.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng #" + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> findAllReaders() {
        return userRepository.findByRoleNameOrderByCreatedAtDesc(Role.READER);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> searchReaders(String keyword, String status, int page, int size) {
        Pageable pageable = PaginationSupport.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String cleanKeyword = keyword != null ? keyword.trim() : null;
        String cleanStatus = null;
        if (status != null && !status.isBlank()) {
            String upper = status.trim().toUpperCase();
            if (!ALLOWED_STATUSES.contains(upper)) {
                throw new IllegalArgumentException("Trạng thái người dùng không hợp lệ: " + status);
            }
            cleanStatus = upper;
        }
        return userRepository.searchByRole(Role.READER, cleanKeyword, cleanStatus, pageable);
    }

    public long countReaders() {
        return userRepository.countByRoleName(Role.READER);
    }

    @Transactional
    public User updateProfile(String username, User updates) {
        User user = findByUsername(username);
        if (updates.getFullName() != null && !updates.getFullName().isBlank()) {
            if (updates.getFullName().trim().length() > 100) {
                throw new IllegalArgumentException("Họ tên không được vượt quá 100 ký tự");
            }
            user.setFullName(updates.getFullName().trim());
        }
        if (updates.getEmail() != null && !updates.getEmail().isBlank()) {
            String newEmail = updates.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
            }
            user.setEmail(newEmail);
        }
        if (updates.getPhone() != null) {
            if (updates.getPhone().trim().length() > 100) {
                throw new IllegalArgumentException("Số điện thoại không được vượt quá 100 ký tự");
            }
            user.setPhone(updates.getPhone().trim());
        }
        if (updates.getAddress() != null) {
            if (updates.getAddress().trim().length() > 100) {
                throw new IllegalArgumentException("Địa chỉ không được vượt quá 100 ký tự");
            }
            user.setAddress(updates.getAddress().trim());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới phải từ 8 ký tự trở lên");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public User toggleStatus(Long userId) {
        User user = findById(userId);
        if (Role.ADMIN.equals(user.getRole().getName())) {
            throw new IllegalStateException("Không thể khóa tài khoản quản trị");
        }
        String next = User.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())
                ? User.STATUS_LOCKED
                : User.STATUS_ACTIVE;
        user.setStatus(next);
        return userRepository.save(user);
    }
}
