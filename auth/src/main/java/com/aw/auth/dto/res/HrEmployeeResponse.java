package com.aw.auth.dto.res;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

// DTO nhận về (Response)
@Data
public class HrEmployeeResponse {
    private UUID Id;
    private String employeeCode;
    private String fullName;
    private String orgUnitCode;
    private String titleCode;
    private String employmentStatus;
    private LocalDateTime createdAt;
}