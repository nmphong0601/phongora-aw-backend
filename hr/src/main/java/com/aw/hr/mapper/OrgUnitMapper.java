package com.aw.hr.mapper;

import com.aw.hr.dto.req.SearchOrgUnitRequest;
import com.aw.hr.dto.req.SearchPagedOrgUnitRequest;
import com.aw.hr.entity.OrgUnitEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface OrgUnitMapper {
    // Lấy danh sách đơn vị tổ chức
    List<OrgUnitEntity> findAll();

    List<OrgUnitEntity> findAllByConditions(SearchOrgUnitRequest searchDto);

    List<OrgUnitEntity> findPagedByConditions(SearchPagedOrgUnitRequest searchPagedDto);

    long countOrgUnits(SearchPagedOrgUnitRequest request);

    OrgUnitEntity findById(@Param("id") UUID id);

    OrgUnitEntity findByCode(@Param("code") String code);

    void insertOrgUnit(OrgUnitEntity orgUnit);

    void updateOrgUnitById(OrgUnitEntity orgUnit);

    void updateOrgUnitByCode(OrgUnitEntity orgUnit);

    // Xóa đơn vị tổ chức (Thường chỉ cho phép xóa nếu đang là DRAFT)
    void deleteById(@Param("id") UUID id);
}