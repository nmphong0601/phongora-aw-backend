package com.aw.hr.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {
    private UUID id;
    private String employeeCode;
    private String fullName;
    private String orgUnitCode;
    private String titleCode;
    private String employeeStatus;
}
