package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateCourseRequest;
import com.manus.digitalecosystem.dto.request.UpdateCourseRequest;
import com.manus.digitalecosystem.dto.response.CourseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {
    CourseResponse createCourse(CreateCourseRequest request, MultipartFile image);

    CourseResponse updateCourse(String courseId, UpdateCourseRequest request);

    CourseResponse updateCourseImage(String courseId, MultipartFile image);

    List<CourseResponse> getAllCourses();

    CourseResponse getCourseById(String courseId);

    List<CourseResponse> getCoursesByDepartment(String departmentId);

    List<CourseResponse> getCoursesByUniversity(String universityId);
}