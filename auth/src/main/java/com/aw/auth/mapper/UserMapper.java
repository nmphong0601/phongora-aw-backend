package com.aw.auth.mapper;

import com.aw.auth.entity.RoleMenu;
import com.aw.auth.entity.User;
import com.aw.auth.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mapper
public interface UserMapper {
    // Lấy thông tin User theo username
    Optional<User> findByUsername(@Param("username") String username);
    boolean existsByUsername(@Param("username") String username);
    boolean existsByEmail(@Param("email") String email);

    // User CRUD
    void insertUser(User user);
    User findById(@Param("id") UUID id);
    void updateUser(User user);
    void deleteUserById(@Param("id") UUID id);

    // User -> Role (Sử dụng UUID)
    void insertUserRole(@Param("userId") UUID userId, @Param("roleName") String roleName);
    void deleteUserRole(@Param("userId") UUID userId, @Param("roleName") String roleName);
    void deleteAllRolesByUserId(@Param("userId") UUID userId);
    List<String> findRolesByUserId(@Param("userId") UUID userId);

    // Role -> Menu (Chỉ sử dụng String)
    void insertRoleMenu(RoleMenu roleMenu);
    void updateRoleMenuPrivileges(RoleMenu roleMenu);
    void deleteRoleMenu(@Param("roleName") String roleName, @Param("menuCode") String menuCode);
    List<RoleMenu> findMenuPrivilegesByRoleName(@Param("roleName") String roleName);

    // Group Roles
    void insertGroupRole(@Param("groupId") Integer groupId, @Param("roleName") String roleName);
    void deleteGroupRole(@Param("groupId") Integer groupId, @Param("roleName") String roleName);
    List<String> findRolesByGroupId(@Param("groupId") Integer groupId);

    // Lấy trực tiếp các Roles của User thông qua bảng user_roles
    @Select("SELECT r.name " +
            "FROM roles r " +
            "JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    Set<String> findRoleNamesByUserId(@Param("userId") UUID userId);

    // (Tùy chọn) Lấy các Roles của một Group nếu bạn cần chức năng quản trị UI
    @Select("SELECT r.name " +
            "FROM roles r " +
            "JOIN group_roles gr ON r.id = gr.role_id " +
            "WHERE gr.group_id = #{groupId}")
    Set<String> findRoleNamesByGroupId(@Param("groupId") Integer groupId);
}
