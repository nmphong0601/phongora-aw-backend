package com.aw.hr.mapper;

import com.aw.hr.dto.res.EmployeeResponse;
import com.aw.hr.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper {
    // Khởi tạo dữ liệu mẫu Employee
    void insertSeedEmployee(EmployeeEntity seedData);
    EmployeeResponse getByCode(String code);
}