package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateStudentProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateStudentProfileRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.StudentResponse;
import com.manus.digitalecosystem.model.VerificationStatus;
import org.springframework.data.domain.Pageable;

public interface StudentService {
    StudentResponse createMyStudent(CreateStudentProfileRequest request);

    StudentResponse getMyStudent();

    StudentResponse updateMyStudent(UpdateStudentProfileRequest request);

    PagedResponse<StudentResponse> searchStudents(
            String q,
            String skill,
            String universityId,
            String departmentId,
            VerificationStatus status,
            Pageable pageable
    );

    StudentResponse getStudentById(String id);

    StudentResponse updateVerificationStatus(String id, VerificationStatus verificationStatus);
}

