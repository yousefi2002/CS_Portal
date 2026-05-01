package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateJobRequest {

    @NotNull(message = "{validation.job.title.required}")
    private LocalizedText title;

    private LocalizedText description;

    private LocalizedText requirements;

    private LocalizedText location;

    private List<LocalizedText> requiredSkills;

    private List<String> applicationRequirements;
}

