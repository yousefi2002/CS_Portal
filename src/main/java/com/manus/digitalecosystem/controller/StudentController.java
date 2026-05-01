package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateStudentAccountRequest;
import com.manus.digitalecosystem.dto.request.DeleteStudentRequest;
import com.manus.digitalecosystem.dto.request.UpdateStudentRequest;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.dto.response.StudentResponse;
import com.manus.digitalecosystem.service.StudentService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/students"})
public class StudentController {

    private final StudentService studentService;
    private final ApiResponseFactory apiResponseFactory;

    public StudentController(StudentService studentService, ApiResponseFactory apiResponseFactory) {
        this.studentService = studentService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentAccountRequest request) {
        return apiResponseFactory.success(HttpStatus.CREATED, "success.student.created", studentService.createStudent(request));
    }

    @PatchMapping("/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<StudentResponse>> updateStudent(@PathVariable String studentId,
                                                                   @Valid @RequestBody UpdateStudentRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.updated", studentService.updateStudent(studentId, request));
    }

    @PatchMapping(value = "/{studentId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<StudentResponse>> updateStudentImage(@PathVariable String studentId,
                                                                        @RequestPart("image") MultipartFile image) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.updated", studentService.updateStudentImage(studentId, image));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<StudentResponse>>> getAllStudents() {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.list", studentService.getAllStudents());
    }

    @GetMapping(params = "universityId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<StudentResponse>>> getStudentsByUniversity(@RequestParam String universityId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.list", studentService.getStudentsByUniversity(universityId));
    }

    @GetMapping(params = "departmentId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<StudentResponse>>> getStudentsByDepartment(@RequestParam String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.list", studentService.getStudentsByDepartment(departmentId));
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<StudentResponse>> getStudentById(@PathVariable String studentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.fetched", studentService.getStudentById(studentId));
    }

    @PatchMapping("/{studentId}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<StudentResponse>> softDeleteStudent(@PathVariable String studentId,
                                                                       @Valid @RequestBody DeleteStudentRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.deleted", studentService.softDeleteStudent(studentId, request));
    }

    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> hardDeleteStudent(@PathVariable String studentId) {
        studentService.hardDeleteStudent(studentId);
        return apiResponseFactory.success(HttpStatus.OK, "success.student.deleted", null);
    }

    @PatchMapping("/{studentId}/graduate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<StudentResponse>> graduateStudent(@PathVariable String studentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.graduated", studentService.markAsGraduated(studentId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<List<StudentResponse>>> searchScoped(@RequestParam(required = false) String q,
                                                                        @RequestParam(required = false) String universityId,
                                                                        @RequestParam(required = false) String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.search", studentService.searchScoped(q, universityId, departmentId));
    }

    @GetMapping("/search/global")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<StudentResponse>>> searchGlobal(@RequestParam(required = false) String q) {
        return apiResponseFactory.success(HttpStatus.OK, "success.student.search", studentService.searchGlobal(q));
    }
}
