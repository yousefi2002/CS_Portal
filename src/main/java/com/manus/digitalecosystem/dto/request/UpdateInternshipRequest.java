package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateInternshipRequest {

    @NotBlank(message = "{validation.internship.title.required}")
    private String title;

    private String description;

    private String requirements;

    private String roadmap;

    private String duration;

    private List<String> requiredSkills;
}

