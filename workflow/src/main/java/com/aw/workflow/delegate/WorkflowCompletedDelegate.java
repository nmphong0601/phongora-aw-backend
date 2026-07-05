package com.aw.workflow.delegate;

import com.aw.workflow.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
        String lastActor = (String) execution.getVariable("lastActor");
        Boolean isApproved = (Boolean) execution.getVariable("approved");

        String status = Boolean.TRUE.equals(isApproved) ? "APPROVED" : "REJECTED";

        // 2. Create the event payload
        WorkflowCompletedEvent event = new WorkflowCompletedEvent(proposalId, status, lastActor);

        // 3. Send the message to Kafka
        kafkaTemplate.send(TOPIC, proposalId, event);

        log.info("Workflow sent message to Kafka Topic [{}]: {}", TOPIC, event);
    }
}
