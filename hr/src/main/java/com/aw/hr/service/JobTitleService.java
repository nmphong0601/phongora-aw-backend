package com.aw.hr.service;

import com.aw.common.dto.res.PageResult;
import com.aw.hr.dto.req.job.title.CreateJobTitleRequest;
import com.aw.hr.dto.req.job.title.SearchJobTitleRequest;
import com.aw.hr.dto.req.job.title.SearchPagedJobTitleRequest;
import com.aw.hr.dto.req.job.title.UpdateJobTitleRequest;
import com.aw.hr.dto.res.job.title.CreateJobTitleResponse;
import com.aw.hr.dto.res.job.title.DeleteJobTitleResponse;
import com.aw.hr.dto.res.job.title.UpdateJobTitleResponse;
import com.aw.hr.entity.JobTitleEntity;

import java.util.List;
import java.util.UUID;

public interface JobTitleService {
    CreateJobTitleResponse create(CreateJobTitleRequest request);
    UpdateJobTitleResponse update(UpdateJobTitleRequest request);
    DeleteJobTitleResponse delete(UUID id);
    List<JobTitleEntity> findAll();
    List<JobTitleEntity> findAllByConditions(SearchJobTitleRequest searchDto);
    PageResult<JobTitleEntity> findPaged(SearchPagedJobTitleRequest searchDto);
    JobTitleEntity findById(UUID id);
    JobTitleEntity findByCode(String code);
}
