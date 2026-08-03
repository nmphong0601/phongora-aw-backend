package com.aw.common.dto.res;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class WorkflowInstanceResponse {
    private String proposalId;
    private String status;
    private String currentStep;
    private String assignee;
    private Map<String, Object> variables;
    private List<Action> actions;
    private Set<String> candidateGroups;
    private Set<String> candidateUsers;
}
