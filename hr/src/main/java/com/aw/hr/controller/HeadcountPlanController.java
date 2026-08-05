package com.aw.hr.controller;

import com.aw.common.response.ApiResponse;
import com.aw.hr.dto.req.headcount.plan.CreateHeadcountPlanRequest;
import com.aw.hr.dto.res.headcount.plan.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.headcount.plan.HeadcountPlanDetailResponse;
import com.aw.hr.entity.HeadcountPlanEntity;
import com.aw.hr.service.HeadcountPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/headcount-plans")
@RequiredArgsConstructor
@Tag(name = "Headcount Plan", description = "Endpoints phục vụ quản lý kế hoạch tuyển dụng nhân sự")
public class HeadcountPlanController {
    private final HeadcountPlanService headcountPlanService;

    @PostMapping("/create")
    public ApiResponse<CreateHeadcountPlanResponse> create(@RequestBody CreateHeadcountPlanRequest request) {
        CreateHeadcountPlanResponse response = headcountPlanService.createHeadcountPlan(request);
        return ApiResponse.success( response);
    }

    @PostMapping("/all")
    public ApiResponse<List<HeadcountPlanEntity>> getAll() {
        List<HeadcountPlanEntity> response = headcountPlanService.findAll();
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<HeadcountPlanDetailResponse> getHeadcountPlanById(@PathVariable UUID id) {
        HeadcountPlanDetailResponse response = headcountPlanService.getHeadcountPlanDetail(id);
        return ApiResponse.success(response);
    }
}
