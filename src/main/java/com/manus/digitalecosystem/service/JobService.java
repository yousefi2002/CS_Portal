package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateJobRequest;
import com.manus.digitalecosystem.dto.request.UpdateJobRequest;
import com.manus.digitalecosystem.dto.response.JobResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface JobService {
    JobResponse createJob(CreateJobRequest request);

    JobResponse updateJob(String id, UpdateJobRequest request);

    void deleteJob(String id);

    JobResponse getJobById(String id);

    PagedResponse<JobResponse> searchJobs(String q, String companyId, Pageable pageable);
}

