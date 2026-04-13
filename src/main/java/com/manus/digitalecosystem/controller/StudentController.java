package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateStudentProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateStudentProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateVerificationStatusRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.StudentResponse;
import com.manus.digitalecosystem.model.VerificationStatus;
import com.manus.digitalecosystem.service.StudentService;
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
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student profile management, verification, and search")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create my student profile (Student)")
    public ResponseEntity<StudentResponse> createMyStudent(@Valid @RequestBody CreateStudentProfileRequest request) {
        StudentResponse response = studentService.createMyStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my student profile (Student)")
    public ResponseEntity<StudentResponse> getMyStudent() {
        return ResponseEntity.ok(studentService.getMyStudent());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update my student profile (Student)")
    public ResponseEntity<StudentResponse> updateMyStudent(@Valid @RequestBody UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(studentService.updateMyStudent(request));
    }

    @GetMapping
    @Operation(summary = "Search students (CRUD + pagination)")
    public ResponseEntity<PagedResponse<StudentResponse>> searchStudents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String universityId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) VerificationStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(studentService.searchStudents(q, skill, universityId, departmentId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by id")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable String id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PatchMapping("/{id}/verification")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN')")
    @Operation(summary = "Verify/reject a student (University Admin / Super Admin)")
    public ResponseEntity<StudentResponse> updateVerificationStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateVerificationStatusRequest request
    ) {
        return ResponseEntity.ok(studentService.updateVerificationStatus(id, request.getVerificationStatus()));
    }
}

