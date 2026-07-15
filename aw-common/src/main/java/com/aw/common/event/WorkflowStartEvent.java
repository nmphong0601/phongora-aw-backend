package com.aw.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStartEvent {
    // The specific Camunda Process ID to trigger (e.g., "Process_Headcount", "Process_LeaveRequest")
    private String processDefinitionKey;

    // The unique ID of your record (e.g., HeadcountPlanId) to link Camunda with your database
    private String businessKey;

    // The user who initiated this request
    private String requesterId;
    private String requesterName;

    // A flexible map to hold any specific data your Camunda process needs (targetHeadcount, reason, amount, etc.)
    private Map<String, Object> variables;
}