package com.aw.hr.service;

import com.aw.common.dto.res.PageResult;
import com.aw.hr.dto.req.CreateOrgUnitRequest;
import com.aw.hr.dto.req.SearchOrgUnitRequest;
import com.aw.hr.dto.req.SearchPagedOrgUnitRequest;
import com.aw.hr.dto.req.UpdateOrgUnitRequest;
import com.aw.hr.dto.res.CreateOrgUnitResponse;
import com.aw.hr.dto.res.DeleteOrgUnitResponse;
import com.aw.hr.dto.res.UpdateOrgUnitResponse;
import com.aw.hr.entity.OrgUnitEntity;

import java.util.List;
import java.util.UUID;

public interface OrgUnitService {
    CreateOrgUnitResponse create(CreateOrgUnitRequest request);
    UpdateOrgUnitResponse update(UpdateOrgUnitRequest request);
    DeleteOrgUnitResponse delete(UUID id);
    List<OrgUnitEntity> findAll();
    List<OrgUnitEntity> findAllByConditions(SearchOrgUnitRequest searchDto);
    PageResult<OrgUnitEntity> findPaged(SearchPagedOrgUnitRequest searchDto);
    OrgUnitEntity findById(UUID id);
    OrgUnitEntity findByCode(String code);
}
