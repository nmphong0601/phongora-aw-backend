package com.aw.workflow.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowInstance {
    private Integer id;
    private String proposalId;
    private String status;
    private String currentStep;
    private String assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
