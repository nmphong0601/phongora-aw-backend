package com.aw.auth.service;

import com.aw.auth.dto.req.LoginRequest;
import com.aw.auth.dto.req.RefreshTokenRequest;
import com.aw.auth.dto.req.RegisterRequest;
import com.aw.auth.dto.res.AuthResponse;
import com.aw.auth.dto.res.UserProfileResponse;
import com.aw.auth.entity.User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface AuthService {
    User findByUsername(String username);
    Set<String> getRolesByUserName(String userName);

    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    UserProfileResponse getUserProfile(String username);
}