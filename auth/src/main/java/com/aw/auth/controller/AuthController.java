package com.aw.auth.controller;

import com.aw.auth.dto.LoginRequest;
import com.aw.auth.model.User;
import com.aw.auth.service.AuthService;
import com.aw.auth.util.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "API quản lý xác thực và phân quyền")
public class AuthController {

    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Operation(summary = "Đăng nhập", description = "Xác thực user và trả về JWT Token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 1. Tìm user thông qua Service
        User user = authService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản không tồn tại!");
        }

        if (!user.getIsActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản đã bị khóa!");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai mật khẩu!");
        }

        // 2. Lấy Roles thông qua Service
        Set<String> roleNames = authService.getRolesByUserId(user.getId());

        String roleString = String.join(",", roleNames);
        String token = jwtProvider.generateToken(user.getId().toString(), roleString);

        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "userId", user.getId().toString(),
                "fullName", user.getFullName(),
                "roles", roleNames
        ));
    }
}
