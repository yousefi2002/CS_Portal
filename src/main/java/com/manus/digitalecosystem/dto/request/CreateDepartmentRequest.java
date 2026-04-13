package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "{validation.department.university_id.required}")
    private String universityId;

    @NotBlank(message = "{validation.department.name.required}")
    private String name;

    private String description;

    private String imageFileId;

    private String adminUserId;
}

