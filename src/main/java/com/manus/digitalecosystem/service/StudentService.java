package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateStudentAccountRequest;
import com.manus.digitalecosystem.dto.request.DeleteStudentRequest;
import com.manus.digitalecosystem.dto.request.UpdateStudentRequest;
import com.manus.digitalecosystem.dto.response.StudentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentService {

    StudentResponse createStudent(CreateStudentAccountRequest request);

    StudentResponse updateStudent(String studentId, UpdateStudentRequest request);

    StudentResponse updateStudentImage(String studentId, MultipartFile image);

    List<StudentResponse> getAllStudents();

    StudentResponse getStudentById(String studentId);

    List<StudentResponse> getStudentsByUniversity(String universityId);

    List<StudentResponse> getStudentsByDepartment(String departmentId);

    StudentResponse softDeleteStudent(String studentId, DeleteStudentRequest request);

    void hardDeleteStudent(String studentId);

    StudentResponse markAsGraduated(String studentId);

    List<StudentResponse> searchScoped(String query, String universityId, String departmentId);

    List<StudentResponse> searchGlobal(String query);
}
