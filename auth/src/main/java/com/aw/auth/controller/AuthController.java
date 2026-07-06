package com.aw.auth.controller;

import com.aw.auth.dto.*;
import com.aw.auth.entity.User;
import com.aw.auth.service.AuthService;
import com.aw.auth.util.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints phục vụ đăng ký, đăng nhập và quản lý phiên (Session)")
public class AuthController {

    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo mới một User với quyền mặc định là ROLE_USER. Kiểm tra trùng lặp Username và Email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng ký thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc tài khoản/email đã tồn tại", content = @Content)
    })
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực tài khoản và mật khẩu. Trả về cặp Access Token và Refresh Token nếu thành công.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác thực thành công",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Sai tài khoản hoặc mật khẩu", content = @Content)
    })
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
                "fullName", user.getFirstName() + " " + user.getLastName(),
                "roles", roleNames
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token", description = "Sử dụng một Refresh Token còn hạn để đổi lấy một Access Token mới mà không cần bắt người dùng đăng nhập lại.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gia hạn Token thành công",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Refresh Token đã hết hạn hoặc không hợp lệ", content = @Content)
    })
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Đăng xuất tài khoản",
            description = "Thu hồi phiên làm việc hiện tại.",
            security = @SecurityRequirement(name = "BearerAuth") // Yêu cầu gửi kèm Bearer Token trên Swagger UI
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "401", description = "Token không hợp lệ hoặc đã hết hạn", content = @Content)
    })
    public ResponseEntity<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        // For standard stateless JWT architectures, clients discard tokens on logout.
        // Optional: Implement a Redis blocklist here to invalidate active tokens early.
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    @Operation(
            summary = "Lấy thông tin profile cá nhân",
            description = "Trích xuất thông tin người dùng hiện tại từ thông tin định danh nằm trong JWT Token.",
            security = @SecurityRequirement(name = "BearerAuth") // Yêu cầu gửi kèm Bearer Token trên Swagger UI
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy dữ liệu thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực / Token không hợp lệ", content = @Content)
    })
    public ResponseEntity<UserProfileResponse> getCurrentUser(Principal principal) {
        // Principal is injected by Spring Security if the access token check is successful
        return ResponseEntity.ok(authService.getUserProfile(principal.getName()));
    }
}
