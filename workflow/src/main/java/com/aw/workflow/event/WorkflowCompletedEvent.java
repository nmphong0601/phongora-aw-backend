package com.aw.workflow.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowCompletedEvent {
    private String proposalId;
    private String status;
    private List<String> users;
    private List<String> roles;
}
