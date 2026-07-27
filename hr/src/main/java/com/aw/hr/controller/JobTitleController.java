package com.aw.hr.controller;

import com.aw.common.dto.res.PageResult;
import com.aw.common.response.ApiResponse;
import com.aw.hr.dto.req.job.title.CreateJobTitleRequest;
import com.aw.hr.dto.req.job.title.SearchJobTitleRequest;
import com.aw.hr.dto.req.job.title.SearchPagedJobTitleRequest;
import com.aw.hr.dto.req.job.title.UpdateJobTitleRequest;
import com.aw.hr.dto.res.job.title.CreateJobTitleResponse;
import com.aw.hr.dto.res.job.title.DeleteJobTitleResponse;
import com.aw.hr.dto.res.job.title.UpdateJobTitleResponse;
import com.aw.hr.entity.JobTitleEntity;
import com.aw.hr.service.JobTitleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-titles")
@RequiredArgsConstructor
@Tag(name = "Job Titles", description = "Endpoints phục vụ quản lý chức vụ")
public class JobTitleController {
    private final JobTitleService jobTitleService;

    @PostMapping("/create")
    public ApiResponse<CreateJobTitleResponse> create(@RequestBody CreateJobTitleRequest request) {
        CreateJobTitleResponse response = jobTitleService.create(request);
        return ApiResponse.success( response);
    }

    @PostMapping("/update")
    public ApiResponse<UpdateJobTitleResponse> update(@RequestBody UpdateJobTitleRequest request) {
        UpdateJobTitleResponse response = jobTitleService.update(request);
        return ApiResponse.success( response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DeleteJobTitleResponse> delete(@PathVariable UUID id) {
        DeleteJobTitleResponse response = jobTitleService.delete(id);
        return ApiResponse.success( response);
    }

    @PostMapping("/all")
    public ApiResponse<List<JobTitleEntity>> getAll() {
        List<JobTitleEntity> response = jobTitleService.findAll();
        return ApiResponse.success(response);
    }

    @PostMapping("/all-with-filters")
    public ApiResponse<List<JobTitleEntity>> getAllWithFilters(@RequestBody SearchJobTitleRequest searchDto) {
        List<JobTitleEntity> response = jobTitleService.findAllByConditions(searchDto);
        return ApiResponse.success(response);
    }

    @PostMapping("/paged")
    public ApiResponse<PageResult<JobTitleEntity>> getPaged(@RequestBody SearchPagedJobTitleRequest searchDto) {
        PageResult<JobTitleEntity> response = jobTitleService.findPaged(searchDto);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}")
    public ApiResponse<JobTitleEntity> getById(@PathVariable UUID id) {
        JobTitleEntity response = jobTitleService.findById(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/{code}")
    public ApiResponse<JobTitleEntity> getByCode(@PathVariable String code) {
        JobTitleEntity response = jobTitleService.findByCode(code);
        return ApiResponse.success(response);
    }
}
