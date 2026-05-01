package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.ApplicationStatus;
import com.manus.digitalecosystem.model.enums.OpportunityType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApplicationResponse {
    private String id;
    private String companyId;
    private String opportunityId;
    private String studentId;
    private OpportunityType opportunityType;
    private ApplicationStatus status;
    private String studentName;
    private String studentImage;
    private String studentEmail;
    private String resumeFileId;
    private String portfolioLink;
    private LocalizedText coverLetter;
    private LocalizedText companyNote;
    private LocalizedText rejectionReason;
    private boolean deleted;
    private Instant reviewedAt;
    private Instant decisionAt;
    private Instant createdAt;
    private Instant updatedAt;
}
