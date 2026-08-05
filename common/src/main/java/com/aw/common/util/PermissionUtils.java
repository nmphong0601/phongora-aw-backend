package com.aw.common.util;

import com.aw.common.security.UserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

public class PermissionUtils {

    private PermissionUtils() {}

    /**
     * Kiểm tra UserPrincipal hiện tại có quyền thao tác Task hay không.
     */
    public static boolean checkWorkflowPermission(
            UserPrincipal currentUser,
            String assignee,
            Set<String> candidateUsers,
            Set<String> candidateGroups) {

        if (currentUser == null || !StringUtils.hasText(currentUser.getEmployeeCode())) {
            return false;
        }

        String employeeCode = currentUser.getEmployeeCode();

        // 1. Nếu Task ĐÃ CÓ Assignee -> Chỉ đúng Assignee mới có quyền
        if (StringUtils.hasText(assignee)) {
            return assignee.equalsIgnoreCase(employeeCode);
        }

        // 2. Kiểm tra Candidate Users (nếu thuộc danh sách user ứng tuyển)
        if (!CollectionUtils.isEmpty(candidateUsers)) {
            boolean isCandidateUser = candidateUsers.stream()
                    .anyMatch(user -> user.equalsIgnoreCase(employeeCode));
            if (isCandidateUser) {
                return true;
            }
        }

        // 3. Kiểm tra Candidate Groups (nếu bất kỳ Role/Authority nào của User khớp với Candidate Group)
        if (!CollectionUtils.isEmpty(candidateGroups) && currentUser.getAuthorities() != null) {
            Set<String> userRoles = currentUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return candidateGroups.stream().anyMatch(group ->
                    userRoles.contains(group)
                            || userRoles.contains("ROLE_" + group)
                            || userRoles.stream().anyMatch(role -> role.equalsIgnoreCase(group))
            );
        }

        return false;
    }
}