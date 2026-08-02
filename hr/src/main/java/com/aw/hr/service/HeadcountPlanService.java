package com.aw.hr.service;

import com.aw.hr.dto.req.CreateHeadcountPlanRequest;
import com.aw.hr.dto.req.PerformWorkflowRequest;
import com.aw.hr.dto.res.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.PerformWorkflowResponse;
import com.aw.hr.dto.res.headcount.plan.HeadcountPlanDetailResponse;
import com.aw.hr.entity.HeadcountPlanEntity;

import java.util.List;
import java.util.UUID;

public interface HeadcountPlanService {
    CreateHeadcountPlanResponse createHeadcountPlan(CreateHeadcountPlanRequest request);
    List<HeadcountPlanEntity> findAll();
    HeadcountPlanDetailResponse getHeadcountPlanDetail(UUID id);
    void updateWorkflowInstanceId(UUID planId, String workflowInstanceId);
    PerformWorkflowResponse performWorkflowTask(PerformWorkflowRequest request);
}
