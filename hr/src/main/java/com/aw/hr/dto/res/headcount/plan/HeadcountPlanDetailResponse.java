package com.aw.hr.dto.res.headcount.plan;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class HeadcountPlanDetailResponse {
    private UUID id;
    private UUID departmentId;
    private UUID titleId;
    private Integer planYear;
    private Integer targetCount;
    private Integer currentCount;

    // Quản lý trạng thái và luồng duyệt
    private String status; // DRAFT, PENDING, APPROVED, REJECTED, RETURNED
    private UUID workflowInstanceId;
    private UUID createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Workflow Data (Lấy từ Camunda) ---
    private String workflowStatus; // Trạng thái WF (VD: PENDING, COMPLETED)
    private String currentTaskName; // Tên bước hiện tại (VD: "Trưởng phòng nhân sự phê duyệt")
    private String assignee; // Người đang được giao xử lý Task (Username/Mã NV)

    // Lưu trữ toàn bộ Variables và Inputs của quy trình để hiển thị lên UI nếu cần
    private Map<String, Object> workflowVariables;
}
