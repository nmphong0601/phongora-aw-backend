package com.aw.auth.service;

import com.aw.auth.model.User;
import java.util.Set;
import java.util.UUID;

public interface AuthService {
    User findByUsername(String username);
    Set<String> getRolesByUserId(UUID userId);
}