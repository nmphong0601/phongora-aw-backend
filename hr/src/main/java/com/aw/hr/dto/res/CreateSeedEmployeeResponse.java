package com.aw.hr.dto.res;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateSeedEmployeeResponse {
    private UUID employeeId;
    private String employeeCode;
}
