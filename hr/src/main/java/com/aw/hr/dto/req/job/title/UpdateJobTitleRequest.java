package com.aw.hr.dto.req.job.title;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateJobTitleRequest {
    private UUID id;
    private String code;
    private String name;
    private UUID jobLevelId;
    private String status;
}
