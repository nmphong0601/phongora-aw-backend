package com.aw.hr.dto.res.job.title;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateJobTitleResponse {
    private UUID id;
    private String code;
    private String name;
    private UUID jobLevelId;
    private String status;
    private LocalDateTime createdAt;
}
