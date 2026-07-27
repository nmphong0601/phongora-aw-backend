package com.aw.hr.mapper;

import com.aw.hr.dto.res.EmployeeResponse;
import com.aw.hr.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.UUID;

@Mapper
public interface EmployeeMapper {
    // Khởi tạo dữ liệu mẫu Employee
    void insertSeedEmployee(EmployeeEntity seedData);
    EmployeeResponse findById(UUID id);
    EmployeeResponse findByCode(String code);
}