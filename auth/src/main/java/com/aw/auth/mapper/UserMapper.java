package com.aw.auth.mapper;

import com.aw.auth.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;
import java.util.UUID;

@Mapper
public interface UserMapper {

    // Lấy thông tin User theo username
    @Select("SELECT id, username, password, email, full_name AS fullName, is_active AS isActive, created_at AS createdAt " +
            "FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

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
