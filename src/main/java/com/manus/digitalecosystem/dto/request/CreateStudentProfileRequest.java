package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateStudentProfileRequest {

    @NotBlank(message = "{validation.student.full_name.required}")
    private String fullName;

    @NotBlank(message = "{validation.student.university_id.required}")
    private String universityId;

    @NotBlank(message = "{validation.student.department_id.required}")
    private String departmentId;

    private String phone;

    private LocalizedText bio;

    private List<LocalizedText> skills;

    private String imageFileId;
}

