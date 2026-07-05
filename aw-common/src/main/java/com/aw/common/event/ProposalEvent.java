package com.aw.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProposalEvent {
    private String proposalId;
    private String action;
    private String triggerBy;
}