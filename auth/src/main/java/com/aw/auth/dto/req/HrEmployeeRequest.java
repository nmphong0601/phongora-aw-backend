package com.aw.auth.dto.req;

import lombok.Data;

// DTO gửi đi (Request)
@Data
public class HrEmployeeRequest {
    private String fullName;
    private String companyCode;
    private String orgUnitCode;
    private String titleCode;
}
