package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateVerificationStatusRequest;
import com.manus.digitalecosystem.dto.response.CompanyResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import com.manus.digitalecosystem.service.CompanyService;
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
@RequestMapping("/api/companies")
@Tag(name = "Companies", description = "Company profile management and search")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Create my company profile (Company Admin)")
    public ResponseEntity<CompanyResponse> createMyCompany(@Valid @RequestBody CreateCompanyProfileRequest request) {
        CompanyResponse response = companyService.createMyCompany(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get my company profile (Company Admin)")
    public ResponseEntity<CompanyResponse> getMyCompany() {
        return ResponseEntity.ok(companyService.getMyCompany());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Update my company profile (Company Admin)")
    public ResponseEntity<CompanyResponse> updateMyCompany(@Valid @RequestBody UpdateCompanyProfileRequest request) {
        return ResponseEntity.ok(companyService.updateMyCompany(request));
    }

    @GetMapping
    @Operation(summary = "Search companies (CRUD + pagination)")
    public ResponseEntity<PagedResponse<CompanyResponse>> searchCompanies(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) VerificationStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(companyService.searchCompanies(q, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by id")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable String id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PatchMapping("/{id}/verification")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Verify/reject a company (Super Admin)")
    public ResponseEntity<CompanyResponse> updateVerificationStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateVerificationStatusRequest request
    ) {
        return ResponseEntity.ok(companyService.updateVerificationStatus(id, request.getVerificationStatus()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete company (Super Admin)")
    public ResponseEntity<Void> deleteCompany(@PathVariable String id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}

