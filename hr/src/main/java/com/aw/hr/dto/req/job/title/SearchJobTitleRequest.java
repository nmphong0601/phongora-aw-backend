package com.aw.hr.dto.req.job.title;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SearchJobTitleRequest {
    private String code;
    private String name;
    private UUID jobLevelId;
    private List<String> statuses;
}
