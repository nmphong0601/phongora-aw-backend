package com.aw.workflow.service;

import com.aw.common.dto.req.PerformWorkflowRequest;
import com.aw.common.dto.res.Action;
import com.aw.common.dto.res.PerformWorkflowResponse;
import com.aw.common.event.WorkflowStartEvent;
import com.aw.common.security.UserPrincipal;
import com.aw.workflow.model.WorkflowInstance;

import java.util.List;

public interface WorkflowService {
    void startWorkflow(WorkflowStartEvent event);
    WorkflowInstance getWorkflowStatus(String proposalId);
    List<Action> getTaskActions(String taskId);
    PerformWorkflowResponse performWorkflowTask(PerformWorkflowRequest request, UserPrincipal currentUser);
}
