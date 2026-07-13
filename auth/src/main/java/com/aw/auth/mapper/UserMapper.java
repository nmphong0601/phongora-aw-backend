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
    // Đếm số lượng user để kiểm tra xem đã cần seed dữ liệu chưa
    @Select("SELECT COUNT(1) FROM users")
    int countUsers();
    void insertUser(User user);
    User findById(@Param("id") UUID id);
    void updateUser(User user);
    void deleteUserById(@Param("id") UUID id);

    // User -> Role (Sử dụng UUID)
    void insertUserRole(@Param("userName") String userName, @Param("roleName") String roleName);
    void deleteUserRole(@Param("userName") String userName, @Param("roleName") String roleName);
    void deleteAllRolesByUserName(@Param("userName") String userName);
    List<String> findRolesByUserName(@Param("userName") String userName);

    // Role -> Menu (Chỉ sử dụng String)
    void insertRoleMenu(RoleMenu roleMenu);
    void updateRoleMenuPrivileges(RoleMenu roleMenu);
    void deleteRoleMenu(@Param("roleName") String roleName, @Param("menuCode") String menuCode);
    List<RoleMenu> findMenuPrivilegesByRoleName(@Param("roleName") String roleName);

    // Group Roles
    void insertGroupRole(@Param("groupName") String groupId, @Param("roleName") String roleName);
    void deleteGroupRole(@Param("groupName") String groupId, @Param("roleName") String roleName);
    List<String> findRolesByGroupName(@Param("groupName") String groupName);

    // Lấy trực tiếp các Roles của User thông qua bảng user_roles
    @Select("SELECT r.name " +
            "FROM roles r " +
            "JOIN user_roles ur ON r.name = ur.role_name " +
            "WHERE ur.user_name = #{username}")
    Set<String> findRoleNamesByUserName(@Param("username") String username);

    // (Tùy chọn) Lấy các Roles của một Group nếu bạn cần chức năng quản trị UI
    @Select("SELECT r.name " +
            "FROM roles r " +
            "JOIN group_roles gr ON r.name = gr.role_name " +
            "WHERE gr.group_name = #{groupName}")
    Set<String> findRoleNamesByGroupName(@Param("groupName") String groupName);
}
