package com.aw.hr.dto.res.headcount.plan;

import com.aw.common.dto.res.WorkflowInstanceResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HeadcountPlanDetailResponse {
    private UUID id;
    private UUID departmentId;
    private UUID titleId;
    private Integer planYear;
    private Integer targetCount;
    private Integer currentCount;

    // Quản lý trạng thái và luồng duyệt
    private String status; // DRAFT, PENDING, APPROVED, REJECTED, RETURNED
    private UUID workflowInstanceId;
    private UUID createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WorkflowInstanceResponse workflowInstance;
}
