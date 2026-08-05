package com.aw.hr.dto.res.headcount.plan;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateHeadcountPlanResponse {
    private UUID id;
    private UUID departmentId;
    private UUID titleId;
    private Integer planYear;
    private Integer targetCount;
    private Integer currentCount;
    private String status;
}
