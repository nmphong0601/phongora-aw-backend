package com.aw.hr.dto.req.headcount.plan;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateHeadcountPlanRequest {
    private UUID departmentId;
    private UUID titleId;
    private Integer planYear;
    private Integer targetCount;
    private Integer currentCount;
    private String status; // DRAFT, PENDING, APPROVED, REJECTED, RETURNED
    private UUID createdBy;
}
