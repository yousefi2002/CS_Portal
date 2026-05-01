package com.manus.digitalecosystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manus.digitalecosystem.dto.request.AssignCoursesToSemesterRequest;
import com.manus.digitalecosystem.dto.request.CreateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.CreateSemesterRequest;
import com.manus.digitalecosystem.dto.request.DeleteDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateSemesterRequest;
import com.manus.digitalecosystem.dto.response.DepartmentResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.model.CurriculumSemester;
import com.manus.digitalecosystem.service.DepartmentService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/departments"})
public class DepartmentController {

    private final DepartmentService departmentService;
    private final ApiResponseFactory apiResponseFactory;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public DepartmentController(DepartmentService departmentService,
                                ApiResponseFactory apiResponseFactory,
                                ObjectMapper objectMapper,
                                Validator validator) {
        this.departmentService = departmentService;
        this.apiResponseFactory = apiResponseFactory;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN')")
    public ResponseEntity<Response<DepartmentResponse>> createDepartment(@RequestPart(value = "data", required = false) String data,
                                                                         @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        CreateDepartmentRequest request = parseRequest(data, CreateDepartmentRequest.class, "error.department.data.required");
        return apiResponseFactory.success(HttpStatus.CREATED, "success.department.created",
                departmentService.createDepartment(request, images));
    }

    @PatchMapping("/{departmentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<DepartmentResponse>> updateDepartment(@PathVariable String departmentId,
                                                                         @Valid @RequestBody UpdateDepartmentRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.department.updated",
                departmentService.updateDepartment(departmentId, request));
    }

    @PatchMapping(value = "/{departmentId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<DepartmentResponse>> updateDepartmentImages(@PathVariable String departmentId,
                                                                               @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return apiResponseFactory.success(HttpStatus.OK, "success.department.updated",
                departmentService.updateDepartmentImages(departmentId, images));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<DepartmentResponse>>> getAllDepartments() {
        return apiResponseFactory.success(HttpStatus.OK, "success.department.list", departmentService.getAllDepartments());
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<DepartmentResponse>> getDepartment(@PathVariable String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.department.fetched",
                departmentService.getDepartmentById(departmentId));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteDepartment(@PathVariable String departmentId) {
        departmentService.deleteDepartment(departmentId);
        return apiResponseFactory.success(HttpStatus.OK, "success.department.deleted", null);
    }

    @PatchMapping("/{departmentId}/delete")
    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    public ResponseEntity<Response<DepartmentResponse>> softDeleteDepartment(@PathVariable String departmentId,
                                                                            @Valid @RequestBody DeleteDepartmentRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.department.updated",
                departmentService.softDeleteDepartment(departmentId, request));
    }

    @PostMapping("/{departmentId}/semesters")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<CurriculumSemester>> createSemester(@PathVariable String departmentId,
                                                                       @Valid @RequestBody CreateSemesterRequest request) {
        return apiResponseFactory.success(HttpStatus.CREATED, "success.semester.created",
                departmentService.createSemester(departmentId, request));
    }

    @PatchMapping("/{departmentId}/semesters/{number}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<CurriculumSemester>> updateSemester(@PathVariable String departmentId,
                                                                       @PathVariable int number,
                                                                       @Valid @RequestBody UpdateSemesterRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.semester.updated",
                departmentService.updateSemester(departmentId, number, request));
    }

    @DeleteMapping("/{departmentId}/semesters/{number}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<Object>> deleteSemester(@PathVariable String departmentId,
                                                           @PathVariable int number) {
        departmentService.deleteSemester(departmentId, number);
        return apiResponseFactory.success(HttpStatus.OK, "success.semester.deleted", null);
    }

    @GetMapping("/{departmentId}/semesters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<CurriculumSemester>>> getSemesters(@PathVariable String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.semester.list",
                departmentService.getSemesters(departmentId));
    }

    @PatchMapping("/{departmentId}/semesters/{number}/courses")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<CurriculumSemester>> assignCoursesToSemester(@PathVariable String departmentId,
                                                                                @PathVariable int number,
                                                                                @Valid @RequestBody AssignCoursesToSemesterRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.semester.updated",
                departmentService.assignCoursesToSemester(departmentId, number, request));
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