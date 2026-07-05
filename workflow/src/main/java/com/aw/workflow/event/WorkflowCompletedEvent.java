package com.aw.workflow.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowCompletedEvent {
    private String proposalId;
    private String status;
    private String approvedBy;
}
