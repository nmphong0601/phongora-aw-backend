package com.aw.hr.dto.req;

import com.aw.common.dto.req.PagedRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchPagedOrgUnitRequest extends PagedRequest {
    private String code;
    private String name;
    private Integer levelId;
    private UUID parentId;
    private UUID managerId;
    private List<String> statuses;
}
