package com.aw.auth.service;

import com.aw.auth.entity.RoleMenu;
import com.aw.auth.entity.User;
import com.aw.auth.entity.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    // ==================== USER CRUD ====================
    User createUser(User user);
    Optional<User> getUserById(UUID id);
    Optional<Optional<User>> getUserByUsername(String username);
    void updateUser(User user);
    void deleteUser(UUID id);

    // ==================== USER -> ROLE ====================
    // Gán Role cho User
    void assignRoleToUser(UUID userId, String roleName);

    // Thu hồi Role của User
    void removeRoleFromUser(UUID userId, String roleName);

    // Lấy danh sách các Role mà User đang giữ
    List<String> getUserRoles(UUID userId);

    // ==================== ROLE -> MENU (PRIVILEGES) ====================
    // Cấu hình đặc quyền cho Role trên một Menu
    void assignMenuPrivilegesToRole(RoleMenu roleMenu);

    // Cập nhật lại 7 cờ đặc quyền của Role
    void updateMenuPrivilegesForRole(RoleMenu roleMenu);

    // Xóa hoàn toàn quyền truy cập Menu của Role
    void removeMenuFromRole(String roleName, String menuCode);

    // Lấy toàn bộ cấu hình Menu và Đặc quyền của một Role
    List<RoleMenu> getRoleMenuPrivileges(String roleName);

    // ==================== GROUP ROLES (Role Bundles) ====================
    // Gán một quyền vào một Group cụ thể
    void assignRoleToGroup(Integer groupId, String roleName);

    // Rút một quyền ra khỏi Group
    void removeRoleFromGroup(Integer groupId, String roleName);

    // Lấy danh sách tất cả các quyền nằm trong một Group
    List<String> getGroupRoles(Integer groupId);
}