package com.aw.hr.service;

import com.aw.hr.dto.req.CreateSeedEmployeeRequest;
import com.aw.hr.dto.res.CreateSeedEmployeeResponse;

public interface EmployeeService {
    CreateSeedEmployeeResponse createSeedEmployee(CreateSeedEmployeeRequest request);
}
