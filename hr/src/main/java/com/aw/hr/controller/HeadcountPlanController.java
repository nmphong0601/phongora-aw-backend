package com.aw.hr.controller;

import com.aw.hr.dto.req.CreateHeadcountPlanRequest;
import com.aw.hr.dto.res.CreateHeadcountPlanResponse;
import com.aw.hr.dto.res.CreateSeedEmployeeResponse;
import com.aw.hr.service.HeadcountPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/headcount-plans")
@RequiredArgsConstructor
@Tag(name = "Headcount Plan", description = "Endpoints phục vụ quản lý kế hoạch tuyển dụng nhân sự")
public class HeadcountPlanController {
    private final HeadcountPlanService headcountPlanService;

    @PostMapping("/create")
    public ResponseEntity<CreateHeadcountPlanResponse> seedEmployee(@RequestBody CreateHeadcountPlanRequest request) {
        CreateHeadcountPlanResponse response = headcountPlanService.createHeadcountPlan(request);
        return ResponseEntity.ok(response);
    }
}
