package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Application;
import com.manus.digitalecosystem.model.ApplicationStatus;
import com.manus.digitalecosystem.model.OpportunityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private String id;
    private String studentId;
    private String companyId;
    private OpportunityType opportunityType;
    private String opportunityId;
    private ApplicationStatus status;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;

    public static ApplicationResponse fromApplication(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .studentId(application.getStudentId())
                .companyId(application.getCompanyId())
                .opportunityType(application.getOpportunityType())
                .opportunityId(application.getOpportunityId())
                .status(application.getStatus())
                .note(application.getNote())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}

