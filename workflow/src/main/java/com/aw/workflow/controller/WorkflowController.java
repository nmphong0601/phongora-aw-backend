package com.aw.workflow.controller;

import com.aw.common.response.ApiResponse;
import com.aw.workflow.dto.ProcessTaskRequest;
import com.aw.workflow.model.WorkflowInstance;
import com.aw.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
@Tag(name = "Workflow Service", description = "API vận hành quy trình phê duyệt công văn/đề xuất sử dụng Camunda Engine")
public class WorkflowController {

    private final WorkflowService workflowService;

    @Operation(summary = "Xem trạng thái quy trình Camunda", description = "Lấy thông tin bước hiện tại trong BPMN và người đang gán xử lý theo ID Đề xuất")
    @GetMapping("/status/{proposalId}")
    public ApiResponse<WorkflowInstance> getStatus(@PathVariable String proposalId) {
        return ApiResponse.success(workflowService.getWorkflowStatus(proposalId));
    }

    @Operation(summary = "Phê duyệt hoặc Từ chối bước hiện tại trong BPMN")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Thông tin xử lý Task của quy trình Camunda"
            )
    })
    @PostMapping("/process")
    public ApiResponse<String> processTask(@RequestBody ProcessTaskRequest request) {
        workflowService.processTask(request);
        return ApiResponse.success("Xử lý bước quy trình Camunda thành công!");
    }
}
