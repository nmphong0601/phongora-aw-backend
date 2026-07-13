package com.aw.auth.service;

import com.aw.auth.entity.RoleMenu;
import com.aw.auth.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface UserService {
    // ==================== USER CRUD ====================
    User createUser(User user);
    Optional<User> getUserById(UUID id);
    Optional<Optional<User>> getUserByUsername(String username);
    void updateUser(User user);
    void deleteUser(UUID id);

    // ==================== USER -> ROLE ====================
    // Gán Role cho User
    void assignRoleToUser(String userName, String roleName);

    // Thu hồi Role của User
    void removeRoleFromUser(String userName, String roleName);

    // Lấy danh sách các Role mà User đang giữ
    List<String> getUserRoles(String userName);

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
    void assignRoleToGroup(String groupName, String roleName);

    // Rút một quyền ra khỏi Group
    void removeRoleFromGroup(String groupName, String roleName);

    // Lấy danh sách tất cả các quyền nằm trong một Group
    List<String> getGroupRoles(String groupName);
}