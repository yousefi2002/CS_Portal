package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateVerificationStatusRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.model.VerificationStatus;
import com.manus.digitalecosystem.service.UniversityService;
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
@RequestMapping("/api/universities")
@Tag(name = "Universities", description = "University profile management and search")
@SecurityRequirement(name = "bearerAuth")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    @Operation(summary = "Create my university profile (University Admin)")
    public ResponseEntity<UniversityResponse> createMyUniversity(@Valid @RequestBody CreateUniversityProfileRequest request) {
        UniversityResponse response = universityService.createMyUniversity(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    @Operation(summary = "Get my university profile (University Admin)")
    public ResponseEntity<UniversityResponse> getMyUniversity() {
        return ResponseEntity.ok(universityService.getMyUniversity());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    @Operation(summary = "Update my university profile (University Admin)")
    public ResponseEntity<UniversityResponse> updateMyUniversity(@Valid @RequestBody UpdateUniversityProfileRequest request) {
        return ResponseEntity.ok(universityService.updateMyUniversity(request));
    }

    @GetMapping
    @Operation(summary = "Search universities (CRUD + pagination)")
    public ResponseEntity<PagedResponse<UniversityResponse>> searchUniversities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) VerificationStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(universityService.searchUniversities(q, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get university by id")
    public ResponseEntity<UniversityResponse> getUniversityById(@PathVariable String id) {
        return ResponseEntity.ok(universityService.getUniversityById(id));
    }

    @PatchMapping("/{id}/verification")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Verify/reject a university (Super Admin)")
    public ResponseEntity<UniversityResponse> updateVerificationStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateVerificationStatusRequest request
    ) {
        return ResponseEntity.ok(universityService.updateVerificationStatus(id, request.getVerificationStatus()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete university (Super Admin)")
    public ResponseEntity<Void> deleteUniversity(@PathVariable String id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.noContent().build();
    }
}

