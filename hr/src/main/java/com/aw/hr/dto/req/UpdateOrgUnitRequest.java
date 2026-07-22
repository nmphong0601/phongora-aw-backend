package com.aw.hr.dto.req;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateOrgUnitRequest {
    private UUID id;
    private String code;
    private String name;
    private Integer levelId;
    private UUID parentId;
    private UUID managerId;
    private String status;
}
