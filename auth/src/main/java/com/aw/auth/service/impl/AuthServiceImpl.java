package com.aw.auth.service.impl;

import com.aw.auth.mapper.UserMapper;
import com.aw.auth.model.User;
import com.aw.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public Set<String> getRolesByUserId(UUID userId) {
        return userMapper.findRoleNamesByUserId(userId);
    }
}
