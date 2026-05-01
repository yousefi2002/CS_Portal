package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.OpportunityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateApplicationRequest {

    @NotBlank(message = "{validation.student.department_id.required}")
    private String studentId;

    @NotNull(message = "{validation.application.opportunity_type.required}")
    private OpportunityType opportunityType;

    @NotBlank(message = "{validation.application.opportunity_id.required}")
    private String opportunityId;

    private String resumeFileId;

    private String portfolioLink;

    private LocalizedText coverLetter;

    private LocalizedText note;
}

