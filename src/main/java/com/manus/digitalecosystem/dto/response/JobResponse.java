package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class JobResponse {
    private String id;
    private String companyId;
    private LocalizedText companyName;
    private LocalizedText companyDescription;
    private String companyImage;
    private LocalizedText jobTitle;
    private LocalizedText jobDescription;
    private double salary;
    private JobType type;
    private int vacancies;
    private List<LocalizedText> requirement;
    private LocalizedText location;
    private List<LocalizedText> requiredSkills;
    private List<String> applicationRequirements;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}
