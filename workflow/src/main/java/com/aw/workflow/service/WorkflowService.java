package com.aw.workflow.service;

import com.aw.workflow.dto.ProcessTaskRequest;
import com.aw.workflow.model.WorkflowInstance;

public interface WorkflowService {
    void startWorkflow(String proposalId);
    WorkflowInstance getWorkflowStatus(String proposalId);
    void processTask(ProcessTaskRequest request);
}
