package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class InternshipResponse {
    private String id;
    private String companyId;
    private LocalizedText companyName;
    private LocalizedText companyDescription;
    private String companyImage;
    private LocalizedText internshipTitle;
    private LocalizedText internshipDescription;
    private LocalizedText requirements;
    private LocalizedText roadmap;
    private LocalizedText duration;
    private int vacancies;
    private List<LocalizedText> requiredSkills;
    private List<String> applicationRequirements;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}
