package com.aw.hr.dto.res;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EmployeeResponse {
    private UUID Id;
    private String employeeCode;
    private String fullName;
    private String orgUnitCode;
    private String titleCode;
    private String employmentStatus;
    private LocalDateTime createdAt;
}
