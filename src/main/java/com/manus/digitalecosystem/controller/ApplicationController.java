package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateApplicationRequest;
import com.manus.digitalecosystem.dto.request.UpdateApplicationStatusRequest;
import com.manus.digitalecosystem.dto.response.ApplicationResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.model.enums.OpportunityType;
import com.manus.digitalecosystem.service.ApplicationService;
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
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Student applications for jobs/internships")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Apply to a job/internship (Student)")
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse response = applicationService.apply(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my applications (Student)")
    public ResponseEntity<PagedResponse<ApplicationResponse>> getMyApplications(Pageable pageable) {
        return ResponseEntity.ok(applicationService.getMyApplications(pageable));
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get applications to my opportunities (Company Admin)")
    public ResponseEntity<PagedResponse<ApplicationResponse>> getCompanyApplications(
            @RequestParam(required = false) OpportunityType opportunityType,
            @RequestParam(required = false) String opportunityId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(applicationService.getCompanyApplications(opportunityType, opportunityId, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update application status (Company Admin / Super Admin)")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateApplicationStatusRequest request
    ) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request));
    }
}

