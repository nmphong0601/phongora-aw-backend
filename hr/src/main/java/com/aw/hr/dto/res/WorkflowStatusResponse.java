package com.aw.hr.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStatusResponse {
    private String proposalId;
    private String status;
    private String currentStep;
    private String assignee;
    private Map<String, Object> variables;
}
