package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private String id;
    private String universityId;
    private String name;
    private String description;
    private String imageFileId;
    private String adminUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public static DepartmentResponse fromDepartment(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .universityId(department.getUniversityId())
                .name(department.getName())
                .description(department.getDescription())
                .imageFileId(department.getImageFileId())
                .adminUserId(department.getAdminUserId())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}

