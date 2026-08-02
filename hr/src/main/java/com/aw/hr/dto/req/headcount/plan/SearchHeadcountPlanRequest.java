package com.aw.hr.dto.req.headcount.plan;

import lombok.Data;

import java.util.UUID;

@Data
public class SearchHeadcountPlanRequest {
    private UUID departmentId;
    private Integer planYear;
}
