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
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manus.digitalecosystem.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/universities"})
public class UniversityController {

    private final UniversityService universityService;
    private final ApiResponseFactory apiResponseFactory;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public UniversityController(UniversityService universityService, ApiResponseFactory apiResponseFactory,
                                ObjectMapper objectMapper, Validator validator) {
        this.universityService = universityService;
        this.apiResponseFactory = apiResponseFactory;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<UniversityResponse>> createUniversity(@RequestPart(value = "data", required = false) String data,
                                                                         @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        if (data == null || data.isBlank()) {
            throw new BadRequestException("error.university.data.required");
        }

        CreateUniversityProfileRequest request;
        try {
            request = objectMapper.readValue(data, CreateUniversityProfileRequest.class);
        } catch (Exception ex) {
            throw new BadRequestException("error.validation.failed");
        }

        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new BadRequestException("error.validation.failed");
        }

        return apiResponseFactory.success(HttpStatus.CREATED, "success.university.created", universityService.createUniversity(request, images));
    }

    @PatchMapping("/{universityId}/data")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN')")
    public ResponseEntity<Response<UniversityResponse>> updateUniversity(@PathVariable String universityId,
                                                                         @Valid @RequestBody UpdateUniversityProfileRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.university.updated",
                universityService.updateUniversity(universityId, request));
    }

    @PatchMapping(value = "/{universityId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN')")
    public ResponseEntity<Response<UniversityResponse>> updateUniversityImages(@PathVariable String universityId,
                                               @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return apiResponseFactory.success(HttpStatus.OK, "success.university.updated",
                universityService.updateUniversityImages(universityId, images));
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

    @GetMapping("/{universityId}/top-students")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<java.util.List<com.manus.digitalecosystem.dto.response.TopStudentResponse>>> getTopStudents(@PathVariable String universityId,
                                                                                                                                    @RequestParam(defaultValue = "10") int limit) {
        return apiResponseFactory.success(HttpStatus.OK, "success.university.top_students",
                universityService.getTopStudents(universityId, limit));
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