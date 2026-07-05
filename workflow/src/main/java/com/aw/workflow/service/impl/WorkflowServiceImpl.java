package com.aw.workflow.service.impl;

import com.aw.workflow.dto.ProcessTaskRequest;
import com.aw.workflow.model.WorkflowInstance;
import com.aw.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
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

    @Override
    public void startWorkflow(String proposalId) {
        log.info("[Camunda] Khởi động Instance mới cho quy trình 'proposal-approval-process' cho ID: {}", proposalId);

        // Thiết lập biến môi trường để truyền vào luồng BPMN
        Map<String, Object> variables = new HashMap<>();
        variables.put("proposalId", proposalId);

        // Khởi chạy quy trình bằng Business Key là proposalId
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("proposal-approval-process", proposalId, variables);
        log.info("[Camunda] Khởi tạo thành công Instance ID: {}, Trạng thái: Hoạt động", pi.getId());
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