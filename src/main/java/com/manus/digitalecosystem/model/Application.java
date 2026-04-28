package com.manus.digitalecosystem.model;

import com.manus.digitalecosystem.model.enums.ApplicationStatus;
import com.manus.digitalecosystem.model.enums.OpportunityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class Application {

    @Id
    private String id;

    @Indexed
    private String companyId;

    @Indexed
    private String opportunityId;

    @Indexed
    private String studentId;

    private OpportunityType opportunityType;

    @Indexed
    private ApplicationStatus status;

    private String studentName;
    private String studentImage;
    private String studentEmail;

    private String resumeFileId;
    private String portfolioLink;

    private LocalizedText coverLetter;
    private LocalizedText companyNote;
    private LocalizedText rejectionReason;

    private Instant reviewedAt;
    private Instant decisionAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

