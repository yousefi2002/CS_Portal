package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateApplicationRequest;
import com.manus.digitalecosystem.dto.request.DeleteApplicationRequest;
import com.manus.digitalecosystem.dto.request.UpdateApplicationRequest;
import com.manus.digitalecosystem.dto.response.ApplicationResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.service.ApplicationService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/applications"})
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApiResponseFactory apiResponseFactory;

    public ApplicationController(ApplicationService applicationService, ApiResponseFactory apiResponseFactory) {
        this.applicationService = applicationService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<ApplicationResponse>> createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        return apiResponseFactory.success(HttpStatus.CREATED, "success.application.created", applicationService.createApplication(request));
    }

    @PatchMapping("/{applicationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<ApplicationResponse>> updateApplication(@PathVariable String applicationId,
                                                                           @Valid @RequestBody UpdateApplicationRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.updated", applicationService.updateApplication(applicationId, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<ApplicationResponse>>> getAllApplications() {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.list", applicationService.getAllApplications());
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<ApplicationResponse>> getApplicationById(@PathVariable String applicationId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.fetched", applicationService.getApplicationById(applicationId));
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<List<ApplicationResponse>>> getApplicationsByCompany(@PathVariable String companyId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.list", applicationService.getApplicationsByCompany(companyId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<ApplicationResponse>>> getApplicationsByStudent(@PathVariable String studentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.list", applicationService.getApplicationsByStudent(studentId));
    }

    @PatchMapping("/{applicationId}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<ApplicationResponse>> softDeleteApplication(@PathVariable String applicationId,
                                                                               @Valid @RequestBody DeleteApplicationRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.deleted", applicationService.softDeleteApplication(applicationId, request));
    }

    @DeleteMapping("/{applicationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteApplication(@PathVariable String applicationId) {
        applicationService.deleteApplication(applicationId);
        return apiResponseFactory.success(HttpStatus.OK, "success.application.deleted", null);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<List<ApplicationResponse>>> searchApplicationsByCompany(@RequestParam String companyId,
                                                                                            @RequestParam(required = false) String q) {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.search", applicationService.searchApplicationsByCompany(companyId, q));
    }

    @GetMapping("/search/global")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<ApplicationResponse>>> searchApplicationsGlobal(@RequestParam(required = false) String q) {
        return apiResponseFactory.success(HttpStatus.OK, "success.application.search", applicationService.searchApplicationsGlobal(q));
    }
}
