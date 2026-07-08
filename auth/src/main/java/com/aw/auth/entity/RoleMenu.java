package com.aw.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenu {
    private String roleName;
    private String menuCode;

    // 7 Đặc quyền của Role này trên Menu
    private boolean canRead;
    private boolean canWrite;
    private boolean canUpdate;
    private boolean canDelete;
    private boolean canApprove;
    private boolean canReturn;
    private boolean canReject;
}
