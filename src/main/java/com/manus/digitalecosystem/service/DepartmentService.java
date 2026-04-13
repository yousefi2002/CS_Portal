package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentCurriculumRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentRequest;
import com.manus.digitalecosystem.dto.response.DepartmentCurriculumResponse;
import com.manus.digitalecosystem.dto.response.DepartmentResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse getMyDepartment();

    DepartmentResponse getDepartmentById(String id);

    PagedResponse<DepartmentResponse> searchDepartments(String q, String universityId, Pageable pageable);

    DepartmentResponse updateDepartment(String id, UpdateDepartmentRequest request);

    void deleteDepartment(String id);

    DepartmentCurriculumResponse getCurriculum(String departmentId);

    DepartmentCurriculumResponse updateCurriculum(String departmentId, UpdateDepartmentCurriculumRequest request);
}

