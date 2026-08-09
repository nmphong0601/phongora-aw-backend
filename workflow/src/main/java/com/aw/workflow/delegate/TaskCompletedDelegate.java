package com.aw.workflow.delegate;

import com.aw.common.event.TaskCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("taskCompletedDelegate")
@RequiredArgsConstructor
@Slf4j
public class TaskCompletedDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "workflow-performed-events";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // 1. Extract variables from the current Camunda workflow instance
        String proposalId = (String) execution.getVariable("proposalId");
        String proposalType = (String) execution.getVariable("proposalType");
        String assignee = (String) execution.getVariable("assignee");
        String user = (String) execution.getVariable("candidateUser");
        String role = (String) execution.getVariable("candidateRoles");
        String action = (String) execution.getVariable("decision");

        String status = "APPROVED";
        status = switch (action) {
            case "APPROVE" -> "APPROVED";
            case "REJECT" -> "REJECTED";
            default -> status;
        };

        // 2. Create the event payload
        TaskCompletedEvent event = new TaskCompletedEvent(
                proposalId,
                proposalType,
                execution.getProcessInstanceId(),
                status,
                assignee,
                user,
                role
        );

        // 3. Send the message to Kafka
        kafkaTemplate.send(TOPIC, proposalId, event);

        log.info("Workflow sent message to Kafka Topic [{}]: {}", TOPIC, event);
    }
}
