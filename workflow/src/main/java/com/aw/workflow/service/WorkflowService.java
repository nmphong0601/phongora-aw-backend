package com.aw.workflow.service;

import com.aw.common.event.WorkflowStartEvent;
import com.aw.workflow.dto.ProcessTaskRequest;
import com.aw.workflow.model.WorkflowInstance;

public interface WorkflowService {
    void startWorkflow(WorkflowStartEvent event);
    WorkflowInstance getWorkflowStatus(String proposalId);
    void processTask(ProcessTaskRequest request);
}
