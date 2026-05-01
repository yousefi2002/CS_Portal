package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.AssignCoursesToSemesterRequest;
import com.manus.digitalecosystem.dto.request.CreateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.CreateSemesterRequest;
import com.manus.digitalecosystem.dto.request.DeleteDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateSemesterRequest;
import com.manus.digitalecosystem.dto.response.DepartmentResponse;
import com.manus.digitalecosystem.model.CurriculumSemester;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request, List<MultipartFile> images);

    DepartmentResponse updateDepartment(String departmentId, UpdateDepartmentRequest request);

    DepartmentResponse updateDepartmentImages(String departmentId, List<MultipartFile> images);

    List<DepartmentResponse> getAllDepartments();

    List<DepartmentResponse> getDepartmentsByUniversity(String universityId);

    DepartmentResponse getDepartmentById(String departmentId);

    void deleteDepartment(String departmentId);

    DepartmentResponse softDeleteDepartment(String departmentId, DeleteDepartmentRequest request);

    CurriculumSemester createSemester(String departmentId, CreateSemesterRequest request);

    CurriculumSemester updateSemester(String departmentId, int number, UpdateSemesterRequest request);

    void deleteSemester(String departmentId, int number);

    List<CurriculumSemester> getSemesters(String departmentId);

    CurriculumSemester assignCoursesToSemester(String departmentId, int number, AssignCoursesToSemesterRequest request);
}