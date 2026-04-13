package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateJobRequest;
import com.manus.digitalecosystem.dto.request.UpdateJobRequest;
import com.manus.digitalecosystem.dto.response.JobResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Job;
import com.manus.digitalecosystem.model.VerificationStatus;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.JobRepository;
import com.manus.digitalecosystem.service.JobService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobServiceImpl(JobRepository jobRepository, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public JobResponse createJob(CreateJobRequest request) {
        Company myCompany = getMyCompanyOrThrow();
        if (myCompany.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new AccessDeniedException("Forbidden");
        }

        Job job = Job.builder()
                .companyId(myCompany.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .location(request.getLocation())
                .requiredSkills(request.getRequiredSkills())
                .build();

        return JobResponse.fromJob(jobRepository.save(job));
    }

    @Override
    public JobResponse updateJob(String id, UpdateJobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.job.not_found", id));

        if (!SecurityUtils.hasRole("SUPER_ADMIN")) {
            Company myCompany = getMyCompanyOrThrow();
            if (!myCompany.getId().equals(job.getCompanyId())) {
                throw new AccessDeniedException("Forbidden");
            }
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setRequiredSkills(request.getRequiredSkills());

        return JobResponse.fromJob(jobRepository.save(job));
    }

    @Override
    public void deleteJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.job.not_found", id));

        if (!SecurityUtils.hasRole("SUPER_ADMIN")) {
            Company myCompany = getMyCompanyOrThrow();
            if (!myCompany.getId().equals(job.getCompanyId())) {
                throw new AccessDeniedException("Forbidden");
            }
        }

        jobRepository.deleteById(id);
    }

    @Override
    public JobResponse getJobById(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.job.not_found", id));
        return JobResponse.fromJob(job);
    }

    @Override
    public PagedResponse<JobResponse> searchJobs(String q, String companyId, Pageable pageable) {
        Page<Job> page;
        if (companyId != null && !companyId.isBlank()) {
            if (q == null || q.isBlank()) {
                page = jobRepository.findByCompanyId(companyId, pageable);
            } else {
                page = jobRepository.findByCompanyIdAndTitleContainingIgnoreCase(companyId, q, pageable);
            }
        } else {
            if (q == null || q.isBlank()) {
                page = jobRepository.findAll(pageable);
            } else {
                page = jobRepository.findByTitleContainingIgnoreCase(q, pageable);
            }
        }

        return PagedResponse.fromPage(page.map(JobResponse::fromJob));
    }

    private Company getMyCompanyOrThrow() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return companyRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.profile.not_found"));
    }
}

