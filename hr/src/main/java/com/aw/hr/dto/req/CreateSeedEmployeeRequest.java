package com.aw.hr.dto.req;

import lombok.Data;

@Data
public class CreateSeedEmployeeRequest {
    private String employeeCode;
    private String fullName;
    private String companyCode;
    private String orgUnitCode;
    private String titleCode;
    // HR có thể tự do thêm các validation annotations như @NotBlank ở đây
}
