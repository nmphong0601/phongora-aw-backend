package com.aw.hr.service.impl;

import com.aw.common.dto.res.PageResult;
import com.aw.hr.dto.req.job.title.CreateJobTitleRequest;
import com.aw.hr.dto.req.job.title.SearchJobTitleRequest;
import com.aw.hr.dto.req.job.title.SearchPagedJobTitleRequest;
import com.aw.hr.dto.req.job.title.UpdateJobTitleRequest;
import com.aw.hr.dto.res.job.title.CreateJobTitleResponse;
import com.aw.hr.dto.res.job.title.DeleteJobTitleResponse;
import com.aw.hr.dto.res.job.title.UpdateJobTitleResponse;
import com.aw.hr.entity.JobTitleEntity;
import com.aw.hr.mapper.JobTitleMapper;
import com.aw.hr.service.JobTitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobTitleServiceImpl implements JobTitleService {
    private final JobTitleMapper jobTitleMapper;

    @Override
    @Transactional
    public CreateJobTitleResponse create(CreateJobTitleRequest request) {

        // Chuẩn bị Entity lưu vào Database
        JobTitleEntity entity = new JobTitleEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setJobLevelId(request.getJobLevelId());
        entity.setStatus(request.getStatus());

        // Lưu vào database
        jobTitleMapper.insertJobTitle(entity);

        // Trả kết quả về cho client
        CreateJobTitleResponse response = new CreateJobTitleResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setJobLevelId(entity.getJobLevelId());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());

        return response;
    }

    @Override
    @Transactional
    public UpdateJobTitleResponse update(UpdateJobTitleRequest request) {

        // Chuẩn bị Entity lưu vào Database
        JobTitleEntity entity = new JobTitleEntity();
        entity.setId(request.getId());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setJobLevelId(request.getJobLevelId());
        entity.setStatus(request.getStatus());

        // Lưu vào database
        jobTitleMapper.updateJobTitleById(entity);

        // Trả kết quả về cho client
        UpdateJobTitleResponse response = new UpdateJobTitleResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setJobLevelId(entity.getJobLevelId());
        response.setStatus(entity.getStatus());

        return response;
    }

    @Override
    @Transactional
    public DeleteJobTitleResponse delete(UUID id) {

        // Xóa item trong database
        jobTitleMapper.deleteById(id);

        // Trả kết quả về cho client
        DeleteJobTitleResponse response = new DeleteJobTitleResponse();
        response.setId(id);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobTitleEntity> findAll() {
        return jobTitleMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobTitleEntity> findAllByConditions(SearchJobTitleRequest searchDto) {
        return jobTitleMapper.findAllByConditions(searchDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<JobTitleEntity> findPaged(SearchPagedJobTitleRequest searchDto) {
        // Fetch total count matching filters
        long totalRows = jobTitleMapper.countJobTitles(searchDto);

        // Optimization: If totalRows is 0, skip the database query
        if (totalRows == 0) {
            return new PageResult<>(Collections.emptyList(), 0, searchDto.getPage(), searchDto.getPageSize());
        }
        // Fetch paginated list using computed offset
        List<JobTitleEntity> items = jobTitleMapper.findPagedByConditions(searchDto);

        return new PageResult<>(items, totalRows, searchDto.getPage(), searchDto.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public JobTitleEntity findById(UUID id) {
        return jobTitleMapper.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public JobTitleEntity findByCode(String code) {
        return jobTitleMapper.findByCode(code);
    }
}
