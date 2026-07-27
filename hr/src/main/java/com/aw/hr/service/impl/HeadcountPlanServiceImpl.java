package com.aw.hr.service.impl;

import com.aw.common.event.ProposalEvent;
import com.aw.common.event.WorkflowStartEvent;
import com.aw.common.security.SecurityUtils;
import com.aw.hr.dto.req.CreateHeadcountPlanRequest;
import com.aw.hr.dto.req.CreateSeedEmployeeRequest;
import com.aw.hr.dto.req.PerformWorkflowRequest;
import com.aw.hr.dto.res.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.CreateSeedEmployeeResponse;
import com.aw.hr.dto.res.PerformWorkflowResponse;
import com.aw.hr.entity.EmployeeEntity;
import com.aw.hr.entity.HeadcountPlanEntity;
import com.aw.hr.mapper.EmployeeMapper;
import com.aw.hr.mapper.HeadcountPlanMapper;
import com.aw.hr.mapper.SequenceMapper;
import com.aw.hr.service.EmployeeService;
import com.aw.hr.service.HeadcountPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeadcountPlanServiceImpl implements HeadcountPlanService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_WORKFLOW_START = "workflow-start-commands";

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
                .processDefinitionKey("headcount_plan_proposal") // Tell Camunda which flow to run
                .businessKey(entity.getId().toString())
                .requesterId(SecurityUtils.getCurrentUserId().toString())
                .requesterName(SecurityUtils.getCurrentUsername())
                .variables(processVars)
                .build();

        kafkaTemplate.send(TOPIC_WORKFLOW_START, event.getBusinessKey(), event);

        return response;
    }

    @Override
    @Transactional
    public PerformWorkflowResponse performWorkflowTask(PerformWorkflowRequest request) {
        PerformWorkflowResponse res = new PerformWorkflowResponse();

        return res;
    }
}
