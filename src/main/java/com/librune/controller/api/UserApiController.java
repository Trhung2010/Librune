package com.librune.controller.api;

import com.librune.entity.User;
import com.librune.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public User getProfile(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @PutMapping("/me")
    public User updateProfile(Authentication authentication, @RequestBody User updates) {
        return userService.updateProfile(authentication.getName(), updates);
    }

    @PutMapping("/me/password")
    public Map<String, String> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        userService.changePassword(
                authentication.getName(),
                body.get("oldPassword"),
                body.get("newPassword")
        );
        return Map.of("message", "Đổi mật khẩu thành công");
    }
}
