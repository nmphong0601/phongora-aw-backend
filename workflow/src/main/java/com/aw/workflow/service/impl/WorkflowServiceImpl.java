package com.aw.workflow.service.impl;

import com.aw.common.dto.req.PerformWorkflowRequest;
import com.aw.common.dto.res.Action;
import com.aw.common.dto.res.PerformWorkflowResponse;
import com.aw.common.event.WorkflowStartEvent;
import com.aw.common.event.WorkflowStartedEvent;
import com.aw.common.security.UserPrincipal;
import com.aw.common.util.PermissionUtils;
import com.aw.workflow.dto.ProcessTaskRequest;
import com.aw.workflow.model.WorkflowInstance;
import com.aw.workflow.service.WorkflowService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.camunda.bpm.admin.impl.plugin.resources.MetricsRestService.objectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

    // Inject Camunda Core Services
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    private final HistoryService historyService;

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

        WorkflowInstance instanceDto = new WorkflowInstance();
        instanceDto.setProposalId(proposalId);

        if (currentTask != null) {
            instanceDto.setStatus("PENDING");
            instanceDto.setCurrentStep(currentTask.getName()); // ví dụ: "Manager xem xét"
            instanceDto.setAssignee(currentTask.getAssignee()); // ví dụ: "Direct Manager"

            // Lấy TẤT CẢ các biến (Bao gồm Process Variables + Local/Input Variables của Task)
            Map<String, Object> allVariables = taskService.getVariables(currentTask.getId());

            // Nếu bạn CHỈ muốn lấy các biến Local/Input của riêng Task này
            // Map<String, Object> localVariables = taskService.getVariablesLocal(currentTask.getId());

            instanceDto.setVariables(allVariables);

            // Lấy danh sách actions của Task hiện tại
            List<Action> actions = getTaskActions(currentTask.getId());
            instanceDto.setActions(actions);

            // Lấy Candidate Users và Candidate Groups từ Camunda IdentityLink
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(currentTask.getId());

            Set<String> candidateGroups = identityLinks.stream()
                    .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()) && link.getGroupId() != null)
                    .map(IdentityLink::getGroupId)
                    .collect(Collectors.toSet());
            instanceDto.setCandidateGroups(candidateGroups);

            Set<String> candidateUsers = identityLinks.stream()
                    .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()) && link.getUserId() != null)
                    .map(IdentityLink::getUserId)
                    .collect(Collectors.toSet());
            instanceDto.setCandidateUsers(candidateUsers);
        } else {
            HistoricProcessInstance historicProcessInstance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceBusinessKey(proposalId)
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (historicProcessInstance == null) {
                instanceDto.setStatus("NOT_FOUND");
                instanceDto.setActions(Collections.emptyList());
                return instanceDto;
            }

            if (historicProcessInstance.getEndTime() != null) {
                instanceDto.setStatus("COMPLETED");
                instanceDto.setCurrentStep("DONE");
                instanceDto.setAssignee(null);

                // Khi quy trình xong, dữ liệu trong RuntimeService sẽ bị xoá, phải lấy từ HistoryService
                List<HistoricVariableInstance> historicVars = historyService
                        .createHistoricVariableInstanceQuery()
                        .processInstanceId(historicProcessInstance.getId())
                        .list();

                Map<String, Object> historyVariables = new HashMap<>();
                for (HistoricVariableInstance var : historicVars) {
                    historyVariables.put(var.getName(), var.getValue());
                }
                instanceDto.setVariables(historyVariables);
            } else {
                instanceDto.setStatus("PROCESSING");
                instanceDto.setCurrentStep("SYSTEM_PROCESSING");
                instanceDto.setActions(Collections.emptyList()); // Đang xử lý tự động -> chưa có user action

                Map<String, Object> runtimeVariables = runtimeService.getVariables(historicProcessInstance.getId());
                instanceDto.setVariables(runtimeVariables);
            }
        }
        return instanceDto;
    }

    @Override
    public List<Action> getTaskActions(String taskId) {
        Object actionsRaw = taskService.getVariable(taskId, "actions");

        if (actionsRaw == null) {
            return Collections.emptyList();
        }

        try {
            // Nếu actions lưu dạng JSON String trong Camunda Variable
            if (actionsRaw instanceof String jsonString) {
                return objectMapper.readValue(jsonString, new TypeReference<>() {});
            }
            // Nếu đã lưu dạng List Object sẵn
            return objectMapper.convertValue(actionsRaw, new TypeReference<>() {});
        } catch (Exception e) {
            // Log error nếu cần
            return Collections.emptyList();
        }
    }

    @Override
    public PerformWorkflowResponse performWorkflowTask(PerformWorkflowRequest request, UserPrincipal currentUser) {

        // ==========================================
        // 1: LẤY TASK VÀ KIỂM TRA QUYỀN
        // ==========================================
        String proposalId = request.getProposalId();
        Task currentTask = taskService.createTaskQuery()
                .processInstanceBusinessKey(proposalId)
                .singleResult();

        if (currentTask == null) {
            throw new IllegalArgumentException("Không tìm thấy tác vụ đang chờ xử lý cho Proposal: " + proposalId);
        }

        // Trích xuất Identity Links từ Camunda
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(currentTask.getId());

        Set<String> candidateGroups = identityLinks.stream()
                .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()) && link.getGroupId() != null)
                .map(IdentityLink::getGroupId)
                .collect(Collectors.toSet());

        Set<String> candidateUsers = identityLinks.stream()
                .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()) && link.getUserId() != null)
                .map(IdentityLink::getUserId)
                .collect(Collectors.toSet());

        // Kiểm tra quyền bằng Utility đã tạo
        boolean hasPermission = PermissionUtils.checkWorkflowPermission(
                currentUser,
                currentTask.getAssignee(),
                candidateUsers,
                candidateGroups
        );

        if (!hasPermission) {
            throw new AccessDeniedException("Bạn không có quyền xử lý tác vụ này!");
        }

        // ==========================================
        // 2: CHUẨN BỊ BIẾN VÀ COMPLETE TASK
        // ==========================================
        Map<String, Object> variables = new HashMap<>();
        variables.put("proposalId", request.getProposalId());
        variables.put("proposalType", request.getProposalType());
        variables.put("decision", request.getAction()); // Biến quan trọng để rẽ nhánh Gateway

        if (StringUtils.hasText(request.getNote())) {
            variables.put("note", request.getNote());
        }

        // Nếu Request có truyền người/nhóm xử lý tiếp theo -> Đẩy vào Variable để Camunda binding cho Task sau
        if (StringUtils.hasText(request.getAssignee())) {
            variables.put("assignee", request.getAssignee());
        }
        if (StringUtils.hasText(request.getCandidateGroup())) {
            variables.put("candidateGroup", request.getCandidateGroup());
        }
        if (StringUtils.hasText(request.getCandidateUser())) {
            variables.put("candidateUser", request.getCandidateUser());
        }

        // Gửi email thông báo
        variables.put("notificationTitle", "");
        variables.put("notificationContent", "");
        variables.put("recipientEmails", "");

        // Ủy quyền (claim)
        taskService.claim(currentTask.getId(), request.getAssignee());

        // Hoàn thành tác vụ hiện tại
        taskService.complete(currentTask.getId(), variables);

        // ==========================================
        // 3: XÁC ĐỊNH TRẠNG THÁI TIẾP THEO
        // ==========================================
        PerformWorkflowResponse response = new PerformWorkflowResponse();

        // Dùng list() thay vì singleResult() vì có thể Workflow rẽ nhánh song song (Parallel Gateway) sinh ra nhiều Task cùng lúc
        List<Task> nextTasks = taskService.createTaskQuery()
                .processInstanceBusinessKey(proposalId)
                .list();

        if (nextTasks != null && !nextTasks.isEmpty()) {
            // Workflow vẫn tiếp tục ở các User Task tiếp theo
            response.setStatus("PENDING");

            Set<String> nextAssignees = new HashSet<>();
            Set<String> nextCandidateGroups = new HashSet<>();
            Set<String> nextCandidateUsers = new HashSet<>();

            for (Task task : nextTasks) {
                if (task.getAssignee() != null) {
                    nextAssignees.add(task.getAssignee());
                }

                List<IdentityLink> nextLinks = taskService.getIdentityLinksForTask(task.getId());

                nextCandidateGroups.addAll(nextLinks.stream()
                        .filter(l -> IdentityLinkType.CANDIDATE.equals(l.getType()) && l.getGroupId() != null)
                        .map(IdentityLink::getGroupId)
                        .collect(Collectors.toSet()));

                nextCandidateUsers.addAll(nextLinks.stream()
                        .filter(l -> IdentityLinkType.CANDIDATE.equals(l.getType()) && l.getUserId() != null)
                        .map(IdentityLink::getUserId)
                        .collect(Collectors.toSet()));
            }

            response.setNextAssignees(nextAssignees);
            response.setNextCandidateGroups(nextCandidateGroups);
            response.setNextCandidateUsers(nextCandidateUsers);

        } else {
            // Nếu không còn User Task nào, kiểm tra xem Tiến trình đã kết thúc chưa
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceBusinessKey(proposalId)
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list().stream().findFirst().orElse(null);

            if (historicProcessInstance != null && historicProcessInstance.getEndTime() != null) {
                response.setStatus("COMPLETED");
            } else {
                response.setStatus("SYSTEM_PROCESSING"); // Đang xử lý tự động (Service Task)
            }

            response.setNextAssignees(Collections.emptySet());
            response.setNextCandidateGroups(Collections.emptySet());
            response.setNextCandidateUsers(Collections.emptySet());
        }

        return response;
    }
}