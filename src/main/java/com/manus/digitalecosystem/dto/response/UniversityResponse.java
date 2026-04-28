package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.UniversityVisibility;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UniversityResponse {
    private String id;
    private LocalizedText name;
    private LocalizedText description;
    private LocalizedText address;
    private UniversityVisibility visibility;
    private String website;
    private String phone;
    private String email;
    private String imageFileId;
    private String adminUserId;
    private VerificationStatus verificationStatus;
    private Instant createdAt;
    private Instant updatedAt;
}