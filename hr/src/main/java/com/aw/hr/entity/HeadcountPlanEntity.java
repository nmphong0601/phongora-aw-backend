package com.aw.hr.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadcountPlanEntity {
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
}
