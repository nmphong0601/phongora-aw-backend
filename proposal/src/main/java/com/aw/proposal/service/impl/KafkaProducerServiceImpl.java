package com.aw.proposal.service.impl;

import com.aw.proposal.service.KafkaProducerService;
import com.aw.common.event.ProposalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "proposal-events";

    public void sendProposalCreatedEvent(String proposalId) {
        ProposalEvent event = new ProposalEvent(proposalId, "CREATED", "system_user");

        // Sends the event to Kafka
        kafkaTemplate.send(TOPIC, event.getProposalId(), event);
        log.info("Message sent to Kafka Topic [{}]: {}", TOPIC, event);
    }
}
