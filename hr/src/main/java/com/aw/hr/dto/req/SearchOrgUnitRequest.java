package com.aw.hr.dto.req;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SearchOrgUnitRequest {
    private String code;
    private String name;
    private Integer levelId;
    private UUID parentId;
    private UUID managerId;
    private List<String> statuses;
}
