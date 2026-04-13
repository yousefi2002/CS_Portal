package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateJobRequest {

    @NotBlank(message = "{validation.job.title.required}")
    private String title;

    private String description;

    private String requirements;

    private String location;

    private List<String> requiredSkills;
}

