package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private String id;
    private String userId;
    private String email;
    private String fullName;
    private String phone;
    private String bio;
    private List<String> skills;
    private String imageFileId;
    private String universityId;
    private String departmentId;
    private VerificationStatus verificationStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public static StudentResponse fromStudent(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .userId(student.getUserId())
                .email(student.getEmail())
                .fullName(student.getFullName())
                .phone(student.getPhone())
                .bio(student.getBio())
                .skills(student.getSkills())
                .imageFileId(student.getImageFileId())
                .universityId(student.getUniversityId())
                .departmentId(student.getDepartmentId())
                .verificationStatus(student.getVerificationStatus())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}

