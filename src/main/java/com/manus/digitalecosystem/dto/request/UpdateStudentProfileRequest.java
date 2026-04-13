package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateStudentProfileRequest {

    @NotBlank(message = "{validation.student.full_name.required}")
    private String fullName;

    private String phone;

    private String bio;

    private List<String> skills;

    private String imageFileId;
}

