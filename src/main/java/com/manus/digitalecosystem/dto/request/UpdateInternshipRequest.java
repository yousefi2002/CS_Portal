package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateInternshipRequest {

    @NotNull(message = "{validation.internship.title.required}")
    private LocalizedText title;

    private LocalizedText description;

    private LocalizedText requirements;

    private LocalizedText roadmap;

    private LocalizedText duration;

    private List<LocalizedText> requiredSkills;
}

