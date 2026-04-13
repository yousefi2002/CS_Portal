package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateJobRequest;
import com.manus.digitalecosystem.dto.request.UpdateJobRequest;
import com.manus.digitalecosystem.dto.response.JobResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job opportunities (CRUD + search + pagination)")
@SecurityRequirement(name = "bearerAuth")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Create a job (Company Admin)")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        JobResponse response = jobService.createJob(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update a job (Company Admin / Super Admin)")
    public ResponseEntity<JobResponse> updateJob(@PathVariable String id, @Valid @RequestBody UpdateJobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete a job (Company Admin / Super Admin)")
    public ResponseEntity<Void> deleteJob(@PathVariable String id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by id")
    public ResponseEntity<JobResponse> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @GetMapping
    @Operation(summary = "Search jobs")
    public ResponseEntity<PagedResponse<JobResponse>> searchJobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String companyId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(jobService.searchJobs(q, companyId, pageable));
    }
}

