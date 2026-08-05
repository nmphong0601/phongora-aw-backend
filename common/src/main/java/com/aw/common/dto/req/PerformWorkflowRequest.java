package com.aw.common.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu xử lý phê duyệt quy trình")
public class PerformWorkflowRequest {
    @Schema(description = "ID của văn bản/đề xuất", example = "53ed57b7-b76c-439e-876d-0a662d41f462")
    private String proposalId;

    @Schema(description = "Loại văn bản/đề xuất", example = "HC_PLAN")
    private String proposalType;

    @Schema(description = "Người thực hiện phê duyệt", example = "AW00001")
    private String assignee;

    @Schema(description = "Nhóm quyền thực hiện phê duyệt", example = "ROLE_HR_EXEC")
    private String candidateGroup;

    @Schema(description = "Người thực hiện phê duyệt", example = "AW00001")
    private String candidateUser;

    @Schema(description = "Hành động phê duyệt", example = "APPROVE", allowableValues = {"APPROVE", "REJECT", "RETURN", "EDIT", "CANCEL"})
    private String action;

    @Schema(description = "Ghi chú của người phê duyệt", example = "Ghi chú")
    private String note;
}
