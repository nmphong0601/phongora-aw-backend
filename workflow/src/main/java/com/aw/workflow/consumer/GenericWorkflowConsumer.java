package com.aw.workflow.consumer;

import com.aw.common.event.WorkflowStartEvent;
import com.aw.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenericWorkflowConsumer {

    private final WorkflowService workflowService;

    @KafkaListener(
            topics = "workflow-start-events",
            groupId = "workflow-group"
    )
    public void consumeWorkflowStartEvent(WorkflowStartEvent event) {
        log.info("Received request to start process: {} for Business Key: {}",
                event.getProcessDefinitionKey(), event.getBusinessKey());

        workflowService.startWorkflow(event);
    }
}