package com.aw.hr.controller;

import com.aw.common.dto.res.PageResult;
import com.aw.common.response.ApiResponse;
import com.aw.hr.dto.req.CreateOrgUnitRequest;
import com.aw.hr.dto.req.SearchOrgUnitRequest;
import com.aw.hr.dto.req.SearchPagedOrgUnitRequest;
import com.aw.hr.dto.req.UpdateOrgUnitRequest;
import com.aw.hr.dto.res.CreateOrgUnitResponse;
import com.aw.hr.dto.res.DeleteOrgUnitResponse;
import com.aw.hr.dto.res.UpdateOrgUnitResponse;
import com.aw.hr.entity.OrgUnitEntity;
import com.aw.hr.service.OrgUnitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org-units")
@RequiredArgsConstructor
@Tag(name = "Organization Unit", description = "Endpoints phục vụ quản lý đơn vị tổ chức")
public class OrgUnitController {
    private final OrgUnitService orgUnitService;

    @PostMapping("/create")
    public ApiResponse<CreateOrgUnitResponse> create(@RequestBody CreateOrgUnitRequest request) {
        CreateOrgUnitResponse response = orgUnitService.create(request);
        return ApiResponse.success( response);
    }

    @PostMapping("/update")
    public ApiResponse<UpdateOrgUnitResponse> update(@RequestBody UpdateOrgUnitRequest request) {
        UpdateOrgUnitResponse response = orgUnitService.update(request);
        return ApiResponse.success( response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DeleteOrgUnitResponse> delete(@PathVariable UUID id) {
        DeleteOrgUnitResponse response = orgUnitService.delete(id);
        return ApiResponse.success( response);
    }

    @PostMapping("/all")
    public ApiResponse<List<OrgUnitEntity>> getAll() {
        List<OrgUnitEntity> response = orgUnitService.findAll();
        return ApiResponse.success(response);
    }

    @PostMapping("/all-with-filters")
    public ApiResponse<List<OrgUnitEntity>> getAllWithFilters(@RequestBody SearchOrgUnitRequest searchDto) {
        List<OrgUnitEntity> response = orgUnitService.findAllByConditions(searchDto);
        return ApiResponse.success(response);
    }

    @PostMapping("/paged")
    public ApiResponse<PageResult<OrgUnitEntity>> getPaged(@RequestBody SearchPagedOrgUnitRequest searchDto) {
        PageResult<OrgUnitEntity> response = orgUnitService.findPaged(searchDto);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}")
    public ApiResponse<OrgUnitEntity> getById(@PathVariable UUID id) {
        OrgUnitEntity response = orgUnitService.findById(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/{code}")
    public ApiResponse<OrgUnitEntity> getByCode(@PathVariable String code) {
        OrgUnitEntity response = orgUnitService.findByCode(code);
        return ApiResponse.success(response);
    }
}
