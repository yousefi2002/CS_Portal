package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateJobRequest;
import com.manus.digitalecosystem.dto.request.DeleteJobRequest;
import com.manus.digitalecosystem.dto.request.UpdateJobRequest;
import com.manus.digitalecosystem.dto.response.JobResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface JobService {

    JobResponse createJob(CreateJobRequest request, MultipartFile image);

    JobResponse updateJobData(String jobId, UpdateJobRequest request);

    JobResponse updateJobImage(String jobId, MultipartFile image);

    List<JobResponse> getAllJobs();

    JobResponse getJobById(String jobId);

    List<JobResponse> searchJobs(String search, String companyId);

    JobResponse softDeleteJob(String jobId, DeleteJobRequest request);

    void deleteJob(String jobId);
}
