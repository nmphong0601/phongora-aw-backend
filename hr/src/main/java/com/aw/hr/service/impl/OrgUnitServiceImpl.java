package com.aw.hr.service.impl;

import com.aw.common.dto.res.PageResult;
import com.aw.hr.dto.req.*;
import com.aw.hr.dto.res.*;
import com.aw.hr.entity.OrgUnitEntity;
import com.aw.hr.mapper.OrgUnitMapper;
import com.aw.hr.service.OrgUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgUnitServiceImpl implements OrgUnitService {
    private final OrgUnitMapper orgUnitMapper;

    @Override
    @Transactional
    public CreateOrgUnitResponse create(CreateOrgUnitRequest request) {

        // Chuẩn bị Entity lưu vào Database
        OrgUnitEntity entity = new OrgUnitEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setLevelId(request.getLevelId());
        entity.setParentId(request.getParentId());
        entity.setManagerId(request.getManagerId());
        entity.setStatus(request.getStatus());

        // Lưu vào database
        orgUnitMapper.insertOrgUnit(entity);

        // Trả kết quả về cho client
        CreateOrgUnitResponse response = new CreateOrgUnitResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setLevelId(entity.getLevelId());
        response.setParentId(entity.getParentId());
        response.setManagerId(entity.getManagerId());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());

        return response;
    }

    @Override
    @Transactional
    public UpdateOrgUnitResponse update(UpdateOrgUnitRequest request) {

        // Chuẩn bị Entity lưu vào Database
        OrgUnitEntity entity = new OrgUnitEntity();
        entity.setId(request.getId());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setLevelId(request.getLevelId());
        entity.setParentId(request.getParentId());
        entity.setManagerId(request.getManagerId());
        entity.setStatus(request.getStatus());

        // Lưu vào database
        orgUnitMapper.updateOrgUnitById(entity);

        // Trả kết quả về cho client
        UpdateOrgUnitResponse response = new UpdateOrgUnitResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setLevelId(entity.getLevelId());
        response.setParentId(entity.getParentId());
        response.setManagerId(entity.getManagerId());
        response.setStatus(entity.getStatus());

        return response;
    }

    @Override
    @Transactional
    public DeleteOrgUnitResponse delete(UUID id) {

        // Xóa item trong database
        orgUnitMapper.deleteById(id);

        // Trả kết quả về cho client
        DeleteOrgUnitResponse response = new DeleteOrgUnitResponse();
        response.setId(id);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitEntity> findAll() {
        return orgUnitMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitEntity> findAllByConditions(SearchOrgUnitRequest searchDto) {
        return orgUnitMapper.findAllByConditions(searchDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OrgUnitEntity> findPaged(SearchPagedOrgUnitRequest searchDto) {
        // Fetch total count matching filters
        long totalRows = orgUnitMapper.countOrgUnits(searchDto);

        // Optimization: If totalRows is 0, skip the database query
        if (totalRows == 0) {
            return new PageResult<>(Collections.emptyList(), 0, searchDto.getPage(), searchDto.getPageSize());
        }
        // Fetch paginated list using computed offset
        List<OrgUnitEntity> items = orgUnitMapper.findPagedByConditions(searchDto);

        return new PageResult<>(items, totalRows, searchDto.getPage(), searchDto.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public OrgUnitEntity findById(UUID id) {
        return orgUnitMapper.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgUnitEntity findByCode(String code) {
        return orgUnitMapper.findByCode(code);
    }
}
