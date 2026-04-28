package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateStudentProfileRequest {

    @NotBlank(message = "{validation.student.full_name.required}")
    private String fullName;

    private String phone;

    private LocalizedText bio;

    private List<LocalizedText> skills;

    private String imageFileId;
}

