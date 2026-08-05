package com.aw.hr.service;

import com.aw.hr.dto.req.headcount.plan.CreateHeadcountPlanRequest;
import com.aw.hr.dto.req.headcount.plan.UpdateHeadcountPlanRequest;
import com.aw.hr.dto.res.headcount.plan.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.headcount.plan.HeadcountPlanDetailResponse;
import com.aw.hr.dto.res.headcount.plan.UpdateHeadcountPlanResponse;
import com.aw.hr.entity.HeadcountPlanEntity;

import java.util.List;
import java.util.UUID;

public interface HeadcountPlanService {
    CreateHeadcountPlanResponse createHeadcountPlan(CreateHeadcountPlanRequest request);
    UpdateHeadcountPlanResponse update(UpdateHeadcountPlanRequest request);
    List<HeadcountPlanEntity> findAll();
    HeadcountPlanDetailResponse getHeadcountPlanDetail(UUID id);
    void updateWorkflowInstanceId(UUID planId, String workflowInstanceId);
    void updateWorkflowStatus(UUID planId, UUID workflowInstanceId, String status);
    HeadcountPlanEntity findById(UUID id);
}
