package com.aw.workflow.consumer;

import com.aw.common.event.ProposalEvent;
import com.aw.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalConsumer {

    private final WorkflowService workflowService;

    @KafkaListener(topics = "proposal-events", groupId = "workflow-group")
    public void consume(ProposalEvent event) {
        log.info("Received Event from Kafka!");
        log.info("Proposal ID: {}", event.getProposalId());
        log.info("Action: {}", event.getAction());

        workflowService.startWorkflow(event.getProposalId());
    }
}
