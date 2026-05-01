package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CompanyResponse {
    private String id;
    private LocalizedText name;
    private LocalizedText description;
    private int numberOfEmployees;
    private LocalizedText developmentType;
    private String website;
    private String phone;
    private String email;
    private List<String> imageFileIds;
    private String adminUserId;
    private VerificationStatus verificationStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
