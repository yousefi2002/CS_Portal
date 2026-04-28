package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.response.Pagination;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.model.enums.UniversityVisibility;
import com.manus.digitalecosystem.service.UniversityService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
@RequestMapping({"/api/v1/universities"})
public class UniversityController {

    private final UniversityService universityService;
    private final ApiResponseFactory apiResponseFactory;

    public UniversityController(UniversityService universityService, ApiResponseFactory apiResponseFactory) {
        this.universityService = universityService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<UniversityResponse>> createUniversity(@Valid @RequestBody CreateUniversityProfileRequest request) {
        return apiResponseFactory.success(HttpStatus.CREATED, "success.university.created", universityService.createUniversity(request));
    }

    @PatchMapping("/{universityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN')")
    public ResponseEntity<Response<UniversityResponse>> updateUniversity(@PathVariable String universityId,
                                                                         @Valid @RequestBody UpdateUniversityProfileRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.university.updated",
                universityService.updateUniversity(universityId, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<UniversityResponse>>> getAllUniversities(@RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        Page<UniversityResponse> universities = universityService.getAllUniversities(page, size);
        Pagination pagination = Pagination.builder()
                .page(universities.getNumber())
                .size(universities.getSize())
                .totalElements(universities.getTotalElements())
                .totalPages(universities.getTotalPages())
                .build();

        return apiResponseFactory.success(HttpStatus.OK, "success.university.list", universities.getContent(), pagination);
    }

    @GetMapping("/{universityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<UniversityResponse>> getUniversity(@PathVariable String universityId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.university.fetched",
                universityService.getUniversityById(universityId));
    }

    @DeleteMapping("/{universityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteUniversity(@PathVariable String universityId) {
        universityService.deleteUniversity(universityId);
        return apiResponseFactory.success(HttpStatus.OK, "success.university.deleted", null);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<UniversityResponse>>> searchUniversities(@RequestParam(required = false) String search,
                                                                                 @RequestParam(required = false) UniversityVisibility visibility,
                                             @RequestParam(defaultValue = "desc") String rank,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Page<UniversityResponse> universities = universityService.searchUniversities(search, visibility, rank, page, size);
        Pagination pagination = Pagination.builder()
            .page(universities.getNumber())
            .size(universities.getSize())
            .totalElements(universities.getTotalElements())
            .totalPages(universities.getTotalPages())
            .build();

        return apiResponseFactory.success(HttpStatus.OK, "success.university.search", universities.getContent(), pagination);
    }
}