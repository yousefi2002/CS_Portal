package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateInternshipRequest;
import com.manus.digitalecosystem.dto.request.UpdateInternshipRequest;
import com.manus.digitalecosystem.dto.response.InternshipResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.service.InternshipService;
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
@RequestMapping("/api/internships")
@Tag(name = "Internships", description = "Internship opportunities (CRUD + search + pagination)")
@SecurityRequirement(name = "bearerAuth")
public class InternshipController {

    private final InternshipService internshipService;

    public InternshipController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Create an internship (Company Admin)")
    public ResponseEntity<InternshipResponse> createInternship(@Valid @RequestBody CreateInternshipRequest request) {
        InternshipResponse response = internshipService.createInternship(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update an internship (Company Admin / Super Admin)")
    public ResponseEntity<InternshipResponse> updateInternship(
            @PathVariable String id,
            @Valid @RequestBody UpdateInternshipRequest request
    ) {
        return ResponseEntity.ok(internshipService.updateInternship(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete an internship (Company Admin / Super Admin)")
    public ResponseEntity<Void> deleteInternship(@PathVariable String id) {
        internshipService.deleteInternship(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get internship by id")
    public ResponseEntity<InternshipResponse> getInternshipById(@PathVariable String id) {
        return ResponseEntity.ok(internshipService.getInternshipById(id));
    }

    @GetMapping
    @Operation(summary = "Search internships")
    public ResponseEntity<PagedResponse<InternshipResponse>> searchInternships(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String companyId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(internshipService.searchInternships(q, companyId, pageable));
    }
}

