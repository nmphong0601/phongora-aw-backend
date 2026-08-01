package com.aw.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStartedEvent {
    private String processDefinitionKey;
    private String businessKey; // Chính là ID của môt Proposal hoặc Request
    private String processInstanceId; // ID của Camunda instance vừa tạo
}