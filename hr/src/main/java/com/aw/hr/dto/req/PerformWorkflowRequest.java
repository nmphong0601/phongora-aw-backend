package com.aw.hr.dto.req;

import lombok.Data;

@Data
public class PerformWorkflowRequest {
    private String actor;
    private String action;
    private String note;
}
