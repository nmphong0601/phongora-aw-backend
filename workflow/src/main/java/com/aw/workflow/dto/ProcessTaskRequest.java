package com.aw.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu xử lý phê duyệt quy trình")
public class ProcessTaskRequest {

    @Schema(description = "ID của văn bản/đề xuất", example = "PROP-12345")
    private String proposalId;

    @Schema(description = "Hành động phê duyệt", example = "APPROVE", allowableValues = {"APPROVE", "REJECT"})
    private String action;

    @Schema(description = "Người thực hiện phê duyệt", example = "manager_01")
    private String actor;
}
