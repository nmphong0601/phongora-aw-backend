package com.aw.auth.service.impl;

import com.aw.auth.dto.req.LoginRequest;
import com.aw.auth.dto.req.RefreshTokenRequest;
import com.aw.auth.dto.req.RegisterRequest;
import com.aw.auth.dto.res.AuthResponse;
import com.aw.auth.dto.res.UserProfileResponse;
import com.aw.auth.entity.User;
import com.aw.auth.mapper.UserMapper;
import com.aw.auth.security.JwtService;
import com.aw.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username).get();
    }

    @Override
    public Set<String> getRolesByUserName(String userName) {
        return userMapper.findRoleNamesByUserName(userName);
    }

    @Override
    @Transactional // Đảm bảo tính toàn vẹn dữ liệu khi insert vào 2 bảng độc lập
    public void register(RegisterRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 1. Tạo thực thể User
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton("ROLE_STAFF"))
                .isActive(true)
                .build();
        user.setId(UUID.randomUUID());

        // 2. Lưu User chính vào DB (ID tự tăng sẽ được đồng bộ vào object 'user')
        userMapper.insertUser(user);

        // 3. Lưu quyền (Roles) liên kết với ID vừa tạo
        for (String role : user.getRoles()) {
            userMapper.insertUserRole(user.getUsername(), role);
        }
    }

    private static UUID getId(User user) {
        return user.getId();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRoles());
        String refreshToken = jwtService.generateToken(user.getUsername(), new HashMap<>(), refreshExpiration);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.getRefreshToken());
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token session"));

        if (jwtService.isTokenValid(request.getRefreshToken(), user.getUsername())) {
            String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getRoles());
            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .tokenType("Bearer")
                    .build();
        }
        throw new IllegalArgumentException("Expired refresh token");
    }

    @Override
    public UserProfileResponse getUserProfile(String username) {
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
