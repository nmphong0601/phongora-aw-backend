package com.aw.hr.service;

import com.aw.hr.dto.req.CreateHeadcountPlanRequest;
import com.aw.hr.dto.req.PerformWorkflowRequest;
import com.aw.hr.dto.res.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.PerformWorkflowResponse;

import java.util.UUID;

public interface HeadcountPlanService {
    CreateHeadcountPlanResponse createHeadcountPlan(CreateHeadcountPlanRequest request);
    void updateWorkflowInstanceId(UUID planId, String workflowInstanceId);
    PerformWorkflowResponse performWorkflowTask(PerformWorkflowRequest request);
}
