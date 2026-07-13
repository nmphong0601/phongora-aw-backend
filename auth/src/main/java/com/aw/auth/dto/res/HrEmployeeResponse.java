package com.aw.auth.dto.res;

import lombok.Data;
import java.util.UUID;

// DTO nhận về (Response)
@Data
public class HrEmployeeResponse {
    private UUID employeeId;
    private String employeeCode; // HR Service tự sinh (ví dụ: AW00006)
}