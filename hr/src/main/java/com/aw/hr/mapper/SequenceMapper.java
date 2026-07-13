package com.aw.hr.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SequenceMapper {

    /**
     * Tăng giá trị sequence lên 1 và trả về giá trị trước khi tăng.
     * Sử dụng RETURNING của PostgreSQL để đảm bảo tính Atomic (không bị race condition).
     */
    @Select("UPDATE employee_code_sequences " +
            "SET next_sequence = next_sequence + 1 " +
            "WHERE company_code = #{companyCode} " +
            "RETURNING next_sequence - 1")
    Integer getNextSequence(@Param("companyCode") String companyCode);
}
