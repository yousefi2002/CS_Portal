package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateJobRequest {

    @NotBlank(message = "{validation.job.company_id.required}")
    private String companyId;

    @NotNull(message = "{validation.job.title.required}")
    private LocalizedText title;

    private LocalizedText description;

    private LocalizedText requirements;

    private LocalizedText location;

    private List<LocalizedText> requiredSkills;
}

