package com.aw.hr.dto.res;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SearchOrgUnitResponse {
    private UUID id;
    private String code;
    private String name;
    private Integer levelId;
    private UUID parentId;
    private UUID managerId;
    private String status;
    private LocalDateTime createdAt;
}
