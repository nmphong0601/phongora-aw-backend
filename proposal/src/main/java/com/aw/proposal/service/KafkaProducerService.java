package com.aw.proposal.service;

public interface KafkaProducerService {
    void sendProposalCreatedEvent(String proposalId);
}
