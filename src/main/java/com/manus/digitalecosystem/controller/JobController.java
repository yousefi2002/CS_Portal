package com.manus.digitalecosystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manus.digitalecosystem.dto.request.CreateJobRequest;
import com.manus.digitalecosystem.dto.request.DeleteJobRequest;
import com.manus.digitalecosystem.dto.request.UpdateJobRequest;
import com.manus.digitalecosystem.dto.response.JobResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.service.JobService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/jobs"})
public class JobController {

    private final JobService jobService;
    private final ApiResponseFactory apiResponseFactory;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public JobController(JobService jobService,
                         ApiResponseFactory apiResponseFactory,
                         ObjectMapper objectMapper,
                         Validator validator) {
        this.jobService = jobService;
        this.apiResponseFactory = apiResponseFactory;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<JobResponse>> createJob(@RequestPart(value = "data", required = false) String data,
                                                           @RequestPart(value = "image", required = false) MultipartFile image) {
        CreateJobRequest request = parseRequest(data, CreateJobRequest.class, "error.job.data.required");
        return apiResponseFactory.success(HttpStatus.CREATED, "success.job.created", jobService.createJob(request, image));
    }

    @PatchMapping("/{jobId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<JobResponse>> updateJobData(@PathVariable String jobId,
                                                               @Valid @RequestBody UpdateJobRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.job.updated", jobService.updateJobData(jobId, request));
    }

    @PatchMapping(value = "/{jobId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<JobResponse>> updateJobImage(@PathVariable String jobId,
                                                                @RequestPart(value = "image", required = false) MultipartFile image) {
        return apiResponseFactory.success(HttpStatus.OK, "success.job.updated", jobService.updateJobImage(jobId, image));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<JobResponse>>> getAllJobs() {
        return apiResponseFactory.success(HttpStatus.OK, "success.job.list", jobService.getAllJobs());
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<JobResponse>> getJobById(@PathVariable String jobId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.job.fetched", jobService.getJobById(jobId));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<JobResponse>>> searchJobs(@RequestParam(required = false) String search,
                                                                  @RequestParam(required = false) String companyId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.job.search", jobService.searchJobs(search, companyId));
    }

    @PatchMapping("/{jobId}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<JobResponse>> softDeleteJob(@PathVariable String jobId,
                                                               @Valid @RequestBody DeleteJobRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.job.deleted", jobService.softDeleteJob(jobId, request));
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteJob(@PathVariable String jobId) {
        jobService.deleteJob(jobId);
        return apiResponseFactory.success(HttpStatus.OK, "success.job.deleted", null);
    }

    private <T> T parseRequest(String data, Class<T> requestType, String errorMessageKey) {
        if (data == null || data.isBlank()) {
            throw new BadRequestException(errorMessageKey);
        }

        try {
            T request = objectMapper.readValue(data, requestType);
            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new BadRequestException("error.validation.failed");
            }
            return request;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("error.validation.failed");
        }
    }
}
