package com.aw.hr.service;

import com.aw.hr.dto.req.CreateHeadcountPlanRequest;
import com.aw.hr.dto.req.PerformWorkflowRequest;
import com.aw.hr.dto.res.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.PerformWorkflowResponse;

public interface HeadcountPlanService {
    CreateHeadcountPlanResponse createHeadcountPlan(CreateHeadcountPlanRequest request);
    PerformWorkflowResponse performWorkflowTask(PerformWorkflowRequest request);
}
