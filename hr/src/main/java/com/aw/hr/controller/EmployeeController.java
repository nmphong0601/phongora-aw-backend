package com.aw.hr.controller;

import com.aw.common.response.ApiResponse;
import com.aw.hr.dto.req.CreateSeedEmployeeRequest;
import com.aw.hr.dto.res.CreateSeedEmployeeResponse;
import com.aw.hr.dto.res.EmployeeResponse;
import com.aw.hr.service.EmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Human Resource", description = "Endpoints phục vụ quản lý nhân sự")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping("/seed")
    public ApiResponse<CreateSeedEmployeeResponse> seedEmployee(@RequestBody CreateSeedEmployeeRequest request) {
        CreateSeedEmployeeResponse response = employeeService.createSeedEmployee(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeResponse> getEmployeeById(@PathVariable UUID id) {
        EmployeeResponse response = employeeService.findById(id);
        return ApiResponse.success(response);
    }

//    @GetMapping("/{code}")
//    public ApiResponse<EmployeeResponse> getEmployeeByCode(@PathVariable String code) {
//        EmployeeResponse response = employeeService.findByCode(code);
//        return ApiResponse.success(response);
//    }
}
