package com.aw.hr.service.impl;

import com.aw.common.dto.res.WorkflowInstanceResponse;
import com.aw.common.event.WorkflowStartEvent;
import com.aw.common.response.ApiResponse;
import com.aw.common.security.SecurityUtils;
import com.aw.common.util.ObjectMapperUtils;
import com.aw.hr.dto.req.headcount.plan.CreateHeadcountPlanRequest;
import com.aw.hr.dto.req.headcount.plan.UpdateHeadcountPlanRequest;
import com.aw.hr.dto.res.headcount.plan.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.headcount.plan.HeadcountPlanDetailResponse;
import com.aw.hr.dto.res.headcount.plan.UpdateHeadcountPlanResponse;
import com.aw.hr.entity.HeadcountPlanEntity;
import com.aw.hr.mapper.HeadcountPlanMapper;
import com.aw.hr.service.HeadcountPlanService;
import com.aw.workflow.model.WorkflowInstance;
import com.aw.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.aw.common.util.PermissionUtils.checkWorkflowPermission;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeadcountPlanServiceImpl implements HeadcountPlanService {
    private final WorkflowService workflowService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_WORKFLOW_START = "workflow-start-events";
    private static final String PROCESS_DEFINITION_KEY = "headcount-plan-approval";

    private final HeadcountPlanMapper headcountPlanMapper;

    @Override
    @Transactional
    public CreateHeadcountPlanResponse createHeadcountPlan(CreateHeadcountPlanRequest request) {

        // Chuẩn bị Entity lưu vào Database
        HeadcountPlanEntity entity = new HeadcountPlanEntity();
        entity.setId(UUID.randomUUID());
        entity.setDepartmentId(request.getDepartmentId());
        entity.setTitleId(request.getTitleId());
        entity.setPlanYear(request.getPlanYear());
        entity.setTargetCount(request.getTargetCount());
        entity.setCurrentCount(request.getCurrentCount());
        entity.setStatus(request.getStatus());
        entity.setCreatedBy(SecurityUtils.getCurrentUserId());

        // Lưu vào database
        headcountPlanMapper.insertHeadcountPlan(entity);

        // Trả kết quả về cho client
        CreateHeadcountPlanResponse response = new CreateHeadcountPlanResponse();
        response.setId(entity.getId());
        response.setDepartmentId(entity.getDepartmentId());
        response.setTitleId(entity.getTitleId());
        response.setPlanYear(entity.getPlanYear());
        response.setTargetCount(entity.getTargetCount());
        response.setCurrentCount(entity.getCurrentCount());
        response.setStatus(entity.getStatus());

        Map<String, Object> processVars = new HashMap<>();
        processVars.put("orgUnitCode", SecurityUtils.getCurrentOrgUnitCode());
        processVars.put("requester", SecurityUtils.getCurrentEmployeeCode());
        processVars.put("action", "CREATE");

        WorkflowStartEvent event = WorkflowStartEvent.builder()
                .processDefinitionKey(PROCESS_DEFINITION_KEY) // Tell Camunda which flow to run
                .businessKey(entity.getId().toString())
                .requesterId(SecurityUtils.getCurrentUserId().toString())
                .requesterName(SecurityUtils.getCurrentUsername())
                .variables(processVars)
                .build();

        kafkaTemplate.send(TOPIC_WORKFLOW_START, event.getBusinessKey(), event);

        return response;
    }

    @Override
    public UpdateHeadcountPlanResponse update(UpdateHeadcountPlanRequest request) {
        // Chuẩn bị Entity lưu vào Database
        HeadcountPlanEntity entity = new HeadcountPlanEntity();
        entity.setId(UUID.randomUUID());
        entity.setDepartmentId(request.getDepartmentId());
        entity.setTitleId(request.getTitleId());
        entity.setPlanYear(request.getPlanYear());
        entity.setTargetCount(request.getTargetCount());
        entity.setCurrentCount(request.getCurrentCount());
        entity.setStatus(request.getStatus());
        entity.setCreatedBy(SecurityUtils.getCurrentUserId());

        // Lưu vào database
        headcountPlanMapper.insertHeadcountPlan(entity);

        // Trả kết quả về cho client
        UpdateHeadcountPlanResponse response = new UpdateHeadcountPlanResponse();
        response.setId(entity.getId());
        response.setDepartmentId(entity.getDepartmentId());
        response.setTitleId(entity.getTitleId());
        response.setPlanYear(entity.getPlanYear());
        response.setTargetCount(entity.getTargetCount());
        response.setCurrentCount(entity.getCurrentCount());
        response.setStatus(entity.getStatus());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeadcountPlanEntity> findAll() {
        return headcountPlanMapper.findAll();
    }

    @Override
    @Transactional
    public HeadcountPlanDetailResponse getHeadcountPlanDetail(UUID id) {
        HeadcountPlanEntity entity = headcountPlanMapper.findById(id);

        // Generic Mapping bằng 1 dòng duy nhất
        HeadcountPlanDetailResponse response = ObjectMapperUtils.map(entity, HeadcountPlanDetailResponse.class);

        // Gọi Workflow lấy dữ liệu động ghép vào
        try {
            WorkflowInstance wf = workflowService.getWorkflowStatus(id.toString());
            if (wf != null) {
                WorkflowInstanceResponse wfInstance = ObjectMapperUtils.map(wf, WorkflowInstanceResponse.class);

                if (!checkWorkflowPermission(
                        SecurityUtils.getCurrentUser(),
                        wfInstance.getAssignee(),
                        wfInstance.getCandidateUsers(),
                        wfInstance.getCandidateGroups())) {
                    wfInstance = null;
                }

                response.setWorkflowInstance(wfInstance);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi lấy thông tin Workflow cho plan {}: {}", id, e.getMessage());
            response.setWorkflowInstance(null);
        }

        return response;
    }

    @Override
    @Transactional
    public void updateWorkflowInstanceId(UUID planId, String workflowInstanceId) {
        headcountPlanMapper.updateWorkflowInstanceId(planId, UUID.fromString(workflowInstanceId));
        log.info("Đã cập nhật workflow_instance_id: {} cho Headcount Plan: {}", workflowInstanceId, planId);
    }

    @Override
    public void updateWorkflowStatus(UUID planId, UUID workflowInstanceId, String status) {
        headcountPlanMapper.updateWorkflowStatus(planId, status, workflowInstanceId);
    }

    @Override
    public HeadcountPlanEntity findById(UUID id) {
        return headcountPlanMapper.findById(id);
    }
}
