package com.aw.workflow.service;

import com.aw.common.dto.res.Action;
import com.aw.common.event.WorkflowStartEvent;
import com.aw.workflow.dto.ProcessTaskRequest;
import com.aw.workflow.model.WorkflowInstance;

import java.util.List;

public interface WorkflowService {
    void startWorkflow(WorkflowStartEvent event);
    WorkflowInstance getWorkflowStatus(String proposalId);
    List<Action> getTaskActions(String taskId);
    void processTask(ProcessTaskRequest request);
}
