package com.manus.digitalecosystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manus.digitalecosystem.dto.request.CreateCourseRequest;
import com.manus.digitalecosystem.dto.request.UpdateCourseRequest;
import com.manus.digitalecosystem.dto.response.CourseResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.service.CourseService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping({"/api/v1/courses"})
public class CourseController {

    private final CourseService courseService;
    private final ApiResponseFactory apiResponseFactory;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public CourseController(CourseService courseService,
                            ApiResponseFactory apiResponseFactory,
                            ObjectMapper objectMapper,
                            Validator validator) {
        this.courseService = courseService;
        this.apiResponseFactory = apiResponseFactory;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<CourseResponse>> createCourse(@RequestPart(value = "data", required = false) String data,
                                                                 @RequestPart(value = "image", required = false) MultipartFile image) {
        CreateCourseRequest request = parseRequest(data, CreateCourseRequest.class, "error.course.data.required");
        return apiResponseFactory.success(HttpStatus.CREATED, "success.course.created",
                courseService.createCourse(request, image));
    }

    @PatchMapping("/{courseId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<CourseResponse>> updateCourse(@PathVariable String courseId,
                                                                @Valid @RequestBody UpdateCourseRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.course.updated",
                courseService.updateCourse(courseId, request));
    }

    @PatchMapping(value = "/{courseId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<CourseResponse>> updateCourseImage(@PathVariable String courseId,
                                                                      @RequestPart(value = "image", required = false) MultipartFile image) {
        return apiResponseFactory.success(HttpStatus.OK, "success.course.updated",
                courseService.updateCourseImage(courseId, image));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<CourseResponse>>> getAllCourses() {
        return apiResponseFactory.success(HttpStatus.OK, "success.course.list", courseService.getAllCourses());
    }

    @GetMapping(params = "departmentId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<CourseResponse>>> getCoursesByDepartment(@RequestParam String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.course.list",
                courseService.getCoursesByDepartment(departmentId));
    }

    @GetMapping(params = "universityId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<CourseResponse>>> getCoursesByUniversity(@RequestParam String universityId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.course.list",
                courseService.getCoursesByUniversity(universityId));
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<CourseResponse>> getCourse(@PathVariable String courseId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.course.fetched",
                courseService.getCourseById(courseId));
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