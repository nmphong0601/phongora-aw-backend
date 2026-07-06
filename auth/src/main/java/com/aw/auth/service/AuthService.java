package com.aw.auth.service;

import com.aw.auth.dto.*;
import com.aw.auth.entity.User;

import java.util.Set;
import java.util.UUID;

public interface AuthService {
    User findByUsername(String username);
    Set<String> getRolesByUserId(UUID userId);

    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    UserProfileResponse getUserProfile(String username);
}