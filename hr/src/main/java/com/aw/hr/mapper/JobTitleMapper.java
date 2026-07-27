package com.aw.hr.mapper;

import com.aw.hr.dto.req.job.title.SearchJobTitleRequest;
import com.aw.hr.dto.req.job.title.SearchPagedJobTitleRequest;
import com.aw.hr.entity.JobTitleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface JobTitleMapper {
    // Lấy danh sách chức vụ
    List<JobTitleEntity> findAll();

    List<JobTitleEntity> findAllByConditions(SearchJobTitleRequest searchDto);

    List<JobTitleEntity> findPagedByConditions(SearchPagedJobTitleRequest searchPagedDto);

    long countJobTitles(SearchPagedJobTitleRequest request);

    JobTitleEntity findById(@Param("id") UUID id);

    JobTitleEntity findByCode(@Param("code") String code);

    void insertJobTitle(JobTitleEntity jobTitle);

    void updateJobTitleById(JobTitleEntity jobTitle);

    void updateJobTitleByCode(JobTitleEntity jobTitle);

    // Xóa chức vụ (Thường chỉ cho phép xóa nếu đang là DRAFT)
    void deleteById(@Param("id") UUID id);
}