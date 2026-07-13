package com.aw.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    // Private constructor để chặn việc dùng từ khóa 'new' khởi tạo object này
    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Lấy toàn bộ Object UserPrincipal
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa xác thực: Không tìm thấy thông tin đăng nhập trong Context");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }

        throw new RuntimeException("Kiểu dữ liệu Principal không hợp lệ. Hệ thống chỉ hỗ trợ UserPrincipal.");
    }

    /**
     * Lấy UUID của User (Bảng users bên Auth)
     */
    public static UUID getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * Lấy Username (Tên đăng nhập)
     */
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * Lấy UUID của Employee (Bảng hr_employees bên HR)
     * Trả về null nếu user này (ví dụ: Admin hệ thống) không có hồ sơ nhân sự
     */
    public static UUID getCurrentEmployeeId() {
        return getCurrentUser().getEmployeeId();
    }

    /**
     * Kiểm tra nhanh xem user có quyền cụ thể nào đó không
     */
    public static boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleName));
    }
}
