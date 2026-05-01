package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateInternshipRequest {

    @NotBlank(message = "{validation.internship.company_id.required}")
    private String companyId;

    @NotNull(message = "{validation.internship.title.required}")
    private LocalizedText title;

    private LocalizedText description;

    private LocalizedText requirements;

    private LocalizedText roadmap;

    private LocalizedText duration;

    private List<LocalizedText> requiredSkills;

    private List<String> applicationRequirements;
}

