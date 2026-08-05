package com.aw.hr.listener;

import com.aw.common.event.TaskCompletedEvent;
import com.aw.common.event.WorkflowStartedEvent;
import com.aw.hr.service.HeadcountPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventListener {

    private final HeadcountPlanService headcountPlanService;
    private static final String PROCESS_DEFINITION_KEY = "headcount-plan-approval";

    @KafkaListener(topics = "workflow-started-events", groupId = "hr-group")
    public void handleWorkflowStarted(WorkflowStartedEvent event) {
        log.info("Nhận được sự kiện workflow-started: {}", event);

        try {
            // Kiểm tra xem event này có đúng là của quy trình headcount-plan không
            if (PROCESS_DEFINITION_KEY.equals(event.getProcessDefinitionKey())) {
                UUID planId = UUID.fromString(event.getBusinessKey());
                String workflowInstanceId = event.getProcessInstanceId();

                // Gọi Service để update DB
                headcountPlanService.updateWorkflowInstanceId(planId, workflowInstanceId);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện workflow-started cho businessKey {}: {}", event.getBusinessKey(), e.getMessage());
            // TODO: Bắn log hệ thống, cảnh báo hoặc đưa vào Dead Letter Queue (DLQ) nếu cần
        }
    }

    @KafkaListener(topics = "workflow-performed-events", groupId = "hr-group")
    public void handleTaskCompleted(TaskCompletedEvent event) {
        log.info("Nhận được sự kiện workflow-performed: {}", event);

        try {
            String proposalType = event.getProposalType();
            if ("HC_PLAN".equals(proposalType)) {
                UUID planId = UUID.fromString(event.getProposalId());
                UUID workflowInstanceId = UUID.fromString(event.getWorkflowInstanceId());
                String status = event.getStatus();
                headcountPlanService.updateWorkflowStatus(planId, workflowInstanceId, status);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện workflow-performed cho businessKey {}: {}", event.getProposalId(), e.getMessage());
            // TODO: Bắn log hệ thống, cảnh báo hoặc đưa vào Dead Letter Queue (DLQ) nếu cần
        }
    }
}
