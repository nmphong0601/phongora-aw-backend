package com.aw.auth.service.impl;

import com.aw.auth.entity.RoleMenu;
import com.aw.auth.entity.User;
import com.aw.auth.entity.UserRole;
import com.aw.auth.mapper.UserMapper;
import com.aw.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    // ==================== USER CRUD ====================
    @Override
    @Transactional
    public User createUser(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        userMapper.insertUser(user);
        return user;
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return Optional.ofNullable(userMapper.findById(id));
    }

    @Override
    public Optional<Optional<User>> getUserByUsername(String username) {
        return Optional.ofNullable(userMapper.findByUsername(username));
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        userMapper.deleteAllRolesByUserId(id);
        userMapper.deleteUserById(id);
    }

    // ==================== USER -> ROLE ====================
    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, String roleName) {
        userMapper.insertUserRole(userId, roleName);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(UUID userId, String roleName) {
        userMapper.deleteUserRole(userId, roleName);
    }

    @Override
    public List<String> getUserRoles(UUID userId) {
        return userMapper.findRolesByUserId(userId);
    }

    // ==================== ROLE -> MENU (PRIVILEGES) ====================
    @Override
    @Transactional
    public void assignMenuPrivilegesToRole(RoleMenu roleMenu) {
        userMapper.insertRoleMenu(roleMenu);
    }

    @Override
    @Transactional
    public void updateMenuPrivilegesForRole(RoleMenu roleMenu) {
        userMapper.updateRoleMenuPrivileges(roleMenu);
    }

    @Override
    @Transactional
    public void removeMenuFromRole(String roleName, String menuCode) {
        userMapper.deleteRoleMenu(roleName, menuCode);
    }

    @Override
    public List<RoleMenu> getRoleMenuPrivileges(String roleName) {
        return userMapper.findMenuPrivilegesByRoleName(roleName);
    }

    // ==================== GROUP ROLES ====================
    @Override
    @Transactional
    public void assignRoleToGroup(Integer groupId, String roleName) {
        userMapper.insertGroupRole(groupId, roleName);
    }

    @Override
    @Transactional
    public void removeRoleFromGroup(Integer groupId, String roleName) {
        userMapper.deleteGroupRole(groupId, roleName);
    }

    @Override
    public List<String> getGroupRoles(Integer groupId) {
        return userMapper.findRolesByGroupId(groupId);
    }
}
