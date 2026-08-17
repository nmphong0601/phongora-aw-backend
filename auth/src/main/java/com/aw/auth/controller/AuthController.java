package com.aw.auth.controller;

import com.aw.common.response.ApiResponse;
import com.aw.auth.dto.req.LoginRequest;
import com.aw.auth.dto.req.RefreshTokenRequest;
import com.aw.auth.dto.req.RegisterRequest;
import com.aw.auth.dto.res.AuthResponse;
import com.aw.auth.dto.res.UserProfileResponse;
import com.aw.auth.service.AuthService;
import com.aw.auth.util.JwtProvider;
import com.aw.common.security.JwtService;
import com.aw.common.security.SecurityUtils;
import com.aw.common.security.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints phục vụ đăng ký, đăng nhập và quản lý phiên (Session)")
public class AuthController {

    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtService jwtService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo mới một User với quyền mặc định là ROLE_USER. Kiểm tra trùng lặp Username và Email.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng ký thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc tài khoản/email đã tồn tại", content = @Content)
    })
    public ApiResponse<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực tài khoản và mật khẩu. Trả về cặp Access Token và Refresh Token nếu thành công.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xác thực thành công",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Sai tài khoản hoặc mật khẩu",
                    content = @Content
            )
    })
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. Authenticate user and generate tokens
        AuthResponse authResponse = authService.login(request);

        // 2. Create the HttpOnly refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("__Host-refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)               // Required for __Host- prefix
                .path("/")                  // Required for __Host- prefix
                .maxAge(7 * 24 * 60 * 60)   // Lifespan (e.g., 7 days)
                .sameSite("Strict")         // Protect against CSRF
                .build();

        // 3. Return access token in body and refresh token in cookie
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 4. Clear raw refresh token from response body
        authResponse.setRefreshToken(null);

        return ApiResponse.success(authResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token", description = "Sử dụng một Refresh Token còn hạn để đổi lấy một Access Token mới mà không cần bắt người dùng đăng nhập lại.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Gia hạn Token thành công",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Refresh Token đã hết hạn hoặc không hợp lệ",
                    content = @Content
            )
    })
    public ApiResponse<AuthResponse> refresh(@CookieValue(name = "__Host-refresh_token") String refreshTokenCookie, HttpServletResponse response) {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshTokenCookie);
        AuthResponse authResponse = authService.refreshToken(request);

        ResponseCookie cookie = ResponseCookie.from("__Host-refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)               // 1. MUST be true
                .path("/")                  // 2. MUST be "/"
                // .domain("...")           // 3. DO NOT call domain(...)
                .maxAge(7 * 24 * 60 * 60)   // Lifespan (e.g., 7 days)
                .sameSite("Strict")         // CSRF protection
                .build();

        // 3. Return access token in body and refresh token in cookie
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 4. Clear raw refresh token from response body
        authResponse.setRefreshToken(null);

        return ApiResponse.success(authResponse);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Đăng xuất tài khoản",
            description = "Thu hồi phiên làm việc hiện tại.",
            security = @SecurityRequirement(name = "BearerAuth") // Yêu cầu gửi kèm Bearer Token trên Swagger UI
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Đăng xuất thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token không hợp lệ hoặc đã hết hạn", content = @Content
            )
    })
    public ApiResponse<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        /// 1. Kiểm tra header hợp lệ
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(400, "Invalid authorization header");
        }

        String token = authHeader.substring(7);

        try {
            // 2. Lấy thời gian hết hạn của token
            long remainingTime = jwtService.getRemainingTime(token);

            // 3. Nếu token còn hạn, đưa vào Redis Blocklist
            if (remainingTime > 0) {
                // Key format: "blocklist:{token}", Value: "true"
                redisTemplate.opsForValue().set(
                        "blocklist:" + token,
                        "true",
                        remainingTime,
                        TimeUnit.MILLISECONDS
                );
            }

            // Note: Client vẫn phải tự xóa token ở LocalStorage/Cookies
            return ApiResponse.success("Logged out successfully");

        } catch (ExpiredJwtException e) {
            // Nếu token đã hết hạn sẵn thì không cần chặn nữa, vẫn báo logout thành công
            return ApiResponse.success("Logged out successfully");
        } catch (Exception e) {
            return ApiResponse.error(500, "Logout failed");
        }
    }

    @GetMapping("/me")
    @Operation(
            summary = "Lấy thông tin profile cá nhân",
            description = "Trích xuất thông tin người dùng hiện tại từ thông tin định danh nằm trong JWT Token.",
            security = @SecurityRequirement(name = "BearerAuth") // Yêu cầu gửi kèm Bearer Token trên Swagger UI
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy dữ liệu thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực / Token không hợp lệ",
                    content = @Content
            )
    })
    public ApiResponse<UserProfileResponse> getCurrentUser(Principal principal) {
        // Principal is injected by Spring Security if the access token check is successful
        return ApiResponse.success(authService.getUserProfile(principal.getName()));
    }
}
