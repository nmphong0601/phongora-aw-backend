package com.aw.auth.mapper;

import com.aw.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mapper
public interface UserMapper {
    // Lấy thông tin User theo username
    Optional<User> findByUsername(@Param("username") String username);
    boolean existsByUsername(@Param("username") String username);
    boolean existsByEmail(@Param("email") String email);

    // Thêm mới User (Sẽ tự trả về ID tự tăng vào object User)
    void insertUser(User user);

    // Thêm role vào bảng quan hệ user_roles
    void insertUserRole(@Param("userId") UUID userId, @Param("role") String role);

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
