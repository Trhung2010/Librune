package com.librune.service;

import com.librune.entity.Role;
import com.librune.entity.User;
import com.librune.repository.RoleRepository;
import com.librune.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

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
        if (input.getPassword() == null || input.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải từ 6 ký tự trở lên");
        }
        if (input.getFullName() == null || input.getFullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
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
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu"));
        if (!User.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu");
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

    public List<User> findAllReaders() {
        return userRepository.findByRoleNameOrderByCreatedAtDesc(Role.READER);
    }

    public long countReaders() {
        return userRepository.countByRoleName(Role.READER);
    }

    @Transactional
    public User updateProfile(String username, User updates) {
        User user = findByUsername(username);
        if (updates.getFullName() != null && !updates.getFullName().isBlank()) {
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
            user.setPhone(updates.getPhone().trim());
        }
        if (updates.getAddress() != null) {
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
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải từ 6 ký tự trở lên");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

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
