package com.aw.workflow.service.impl;

import com.aw.common.event.WorkflowStartEvent;
import com.aw.common.event.WorkflowStartedEvent;
import com.aw.workflow.dto.ProcessTaskRequest;
import com.aw.workflow.model.WorkflowInstance;
import com.aw.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

    // Inject Camunda Core Services
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_WORKFLOW_STARTED = "workflow-started-events";

    @Override
    public void startWorkflow(WorkflowStartEvent event) {
        try {
            log.info("[Camunda] Khởi động Instance mới cho quy trình {} cho ID: {}",
                    event.getProcessDefinitionKey(),
                    event.getBusinessKey()
            );

            // 1. Retrieve the variables injected by the producer
            Map<String, Object> variables = event.getVariables();

            // 2. Automatically inject standard system variables (so you don't have to pass them manually every time)
            variables.put("initiatorId", event.getRequesterId());
            variables.put("initiatorName", event.getRequesterName());

            // 3. Start the process dynamically based on the definition key
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    event.getProcessDefinitionKey(),
                    event.getBusinessKey(),
                    variables
            );

            log.info("[Camunda] Khởi tạo thành công Instance ID: {}, Trạng thái: Hoạt động", processInstance.getId());

            // 4. Bắn sự kiện báo cho HR Service biết Workflow đã được tạo thành công
            WorkflowStartedEvent startedEvent = WorkflowStartedEvent.builder()
                    .processDefinitionKey(event.getProcessDefinitionKey())
                    .businessKey(event.getBusinessKey())
                    .processInstanceId(processInstance.getId())
                    .build();

            kafkaTemplate.send(TOPIC_WORKFLOW_STARTED, event.getBusinessKey(), startedEvent);
            log.info("[Kafka] Đã gửi WorkflowStartedEvent cho businessKey: {}", event.getBusinessKey());
        } catch (Exception ex) {
            log.info("[startWorkflow] Đã có lỗi xảy ra : {}", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public WorkflowInstance getWorkflowStatus(String proposalId) {
        // Truy vấn Task hiện tại đang chờ xử lý của Proposal này
        Task currentTask = taskService.createTaskQuery()
                .processInstanceBusinessKey(proposalId)
                .singleResult();

        WorkflowInstance statusDto = new WorkflowInstance();
        statusDto.setProposalId(proposalId);

        if (currentTask != null) {
            statusDto.setStatus("PENDING");
            statusDto.setCurrentStep(currentTask.getName()); // ví dụ: "Manager xem xét"
            statusDto.setAssignee(currentTask.getAssignee()); // ví dụ: "Direct Manager"
        } else {
            // Nếu không còn Task nào hoạt động nghĩa là quy trình đã hoàn thành hoặc bị từ chối
            // Chúng ta có thể query bảng lịch sử ACT_HI_PROCINST của Camunda để lấy trạng thái cuối cùng
            boolean isCompleted = runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(proposalId)
                    .active()
                    .count() == 0;

            if (isCompleted) {
                statusDto.setStatus("COMPLETED");
                statusDto.setCurrentStep("DONE");
                statusDto.setAssignee(null);
            }
        }
        return statusDto;
    }

    @Override
    public void processTask(ProcessTaskRequest request) {
        log.info("[Camunda] Tiến hành giải quyết Task cho Proposal: {}, Hành động: {}, Người xử lý: {}",
                request.getProposalId(), request.getAction(), request.getActor());

        // 1. Tìm Task đang chờ của quy trình dựa trên businessKey (proposalId)
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(request.getProposalId())
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Không tìm thấy bước quy trình nào đang chờ phê duyệt cho Đề xuất này!");
        }

        // 2. Định nghĩa các biến quyết định hướng đi trong BPMN (approved = true/false)
        Map<String, Object> variables = new HashMap<>();
        boolean isApproved = "APPROVE".equalsIgnoreCase(request.getAction());
        variables.put("approved", isApproved);
        variables.put("lastActor", request.getActor());

        // 3. Ủy quyền (claim) và hoàn thành (complete) Task để Camunda chạy tiếp luồng sang Gateway
        taskService.claim(task.getId(), request.getActor());
        taskService.complete(task.getId(), variables);

        log.info("[Camunda] Đã hoàn thành Task: '{}'. Quy trình được tiếp tục điều hướng tự động.", task.getName());
    }
}