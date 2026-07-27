package com.aw.hr.service;

import com.aw.hr.dto.req.CreateSeedEmployeeRequest;
import com.aw.hr.dto.res.CreateSeedEmployeeResponse;
import com.aw.hr.dto.res.EmployeeResponse;

import java.util.UUID;

public interface EmployeeService {
    CreateSeedEmployeeResponse createSeedEmployee(CreateSeedEmployeeRequest request);
    EmployeeResponse findById(UUID id);
    EmployeeResponse findByCode(String code);
}
