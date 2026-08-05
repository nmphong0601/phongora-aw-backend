package com.aw.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletedEvent {
    private String proposalId;
    private String proposalType;
    private String workflowInstanceId;
    private String status;
    private String assignee;
    private String user;
    private String role;
}
