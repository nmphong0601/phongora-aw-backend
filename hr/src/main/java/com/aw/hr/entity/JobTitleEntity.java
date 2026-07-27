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
public class JobTitleEntity {
    private UUID id;
    private String code;
    private String name;
    private UUID jobLevelId;
    private String status;
    private LocalDateTime createdAt;
}
