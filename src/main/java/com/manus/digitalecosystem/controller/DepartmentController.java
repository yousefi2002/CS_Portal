package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.CreateDepartmentAdminAccountRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentCurriculumRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentRequest;
import com.manus.digitalecosystem.dto.response.DepartmentCurriculumResponse;
import com.manus.digitalecosystem.dto.response.DepartmentResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.UserResponse;
import com.manus.digitalecosystem.service.DepartmentService;
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
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Department profile + curriculum management and search")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN')")
    @Operation(summary = "Create a department (University Admin / Super Admin)")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DEPARTMENT_ADMIN')")
    @Operation(summary = "Get my department (Department Admin)")
    public ResponseEntity<DepartmentResponse> getMyDepartment() {
        return ResponseEntity.ok(departmentService.getMyDepartment());
    }

    @GetMapping
    @Operation(summary = "Search departments (CRUD + pagination)")
    public ResponseEntity<PagedResponse<DepartmentResponse>> searchDepartments(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(departmentService.searchDepartments(q, universityId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by id")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable String id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Update department profile (Department Admin / University Admin / Super Admin)")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable String id,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN')")
    @Operation(summary = "Delete department (University Admin / Super Admin)")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/curriculum")
    @Operation(summary = "Get department curriculum")
    public ResponseEntity<DepartmentCurriculumResponse> getCurriculum(@PathVariable String id) {
        return ResponseEntity.ok(departmentService.getCurriculum(id));
    }

    @PutMapping("/{id}/curriculum")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Update department curriculum (Department Admin / University Admin / Super Admin)")
    public ResponseEntity<DepartmentCurriculumResponse> updateCurriculum(
            @PathVariable String id,
            @RequestBody UpdateDepartmentCurriculumRequest request
    ) {
        return ResponseEntity.ok(departmentService.updateCurriculum(id, request));
    }

    @PostMapping("/{id}/admin-account")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN')")
    @Operation(summary = "Create department admin account and assign to department (University Admin / Super Admin)")
    public ResponseEntity<UserResponse> createDepartmentAdminAccount(
            @PathVariable String id,
            @Valid @RequestBody CreateDepartmentAdminAccountRequest request
    ) {
        UserResponse response = departmentService.createDepartmentAdminAccount(id, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
