package com.aw.common.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowVariables {
    private String taskId;
    private String taskName;
    private List<Action> actions;
}