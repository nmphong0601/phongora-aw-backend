package com.aw.hr.dto.req.job.title;

import com.aw.common.dto.req.PagedRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchPagedJobTitleRequest extends PagedRequest {
    private String code;
    private String name;
    private UUID jobLevelId;
    private List<String> statuses;
}
