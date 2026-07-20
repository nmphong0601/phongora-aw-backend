package com.aw.workflow.delegate;

import com.aw.workflow.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("workflowCompletedDelegate")
@RequiredArgsConstructor
@Slf4j
public class WorkflowCompletedDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "workflow-events";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // 1. Extract variables from the current Camunda workflow instance
        String proposalId = (String) execution.getVariable("proposalId");
        List<String> users = List.of((String) execution.getVariable("candidateUsers"));
        List<String> roles = List.of((String) execution.getVariable("candidateRoles"));
        String action = (String) execution.getVariable("action");

        String status = "APPROVED";
        status = switch (action) {
            case "APPROVE" -> "APPROVED";
            case "REJECT" -> "REJECTED";
            default -> status;
        };

        // 2. Create the event payload
        WorkflowCompletedEvent event = new WorkflowCompletedEvent(proposalId, status, users, roles);

        // 3. Send the message to Kafka
        kafkaTemplate.send(TOPIC, proposalId, event);

        log.info("Workflow sent message to Kafka Topic [{}]: {}", TOPIC, event);
    }
}
