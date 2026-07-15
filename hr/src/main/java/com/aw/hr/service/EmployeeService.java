package com.aw.hr.service;

import com.aw.hr.dto.req.CreateSeedEmployeeRequest;
import com.aw.hr.dto.res.CreateSeedEmployeeResponse;
import com.aw.hr.dto.res.EmployeeResponse;

public interface EmployeeService {
    CreateSeedEmployeeResponse createSeedEmployee(CreateSeedEmployeeRequest request);
    EmployeeResponse findByCode(String code);
}
