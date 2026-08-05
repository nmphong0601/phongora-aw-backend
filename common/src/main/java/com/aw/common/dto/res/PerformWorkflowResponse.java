package com.aw.common.dto.res;

import lombok.Data;

import java.util.Set;

@Data
public class PerformWorkflowResponse {
    private String status;
    private Set<String> nextAssignees;
    private Set<String> nextCandidateGroups;
    private Set<String> nextCandidateUsers;
}
