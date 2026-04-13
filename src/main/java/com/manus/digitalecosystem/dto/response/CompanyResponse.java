package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private String id;
    private String name;
    private String description;
    private String developmentType;
    private String achievements;
    private String website;
    private String phone;
    private String email;
    private String imageFileId;
    private String adminUserId;
    private VerificationStatus verificationStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public static CompanyResponse fromCompany(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .developmentType(company.getDevelopmentType())
                .achievements(company.getAchievements())
                .website(company.getWebsite())
                .phone(company.getPhone())
                .email(company.getEmail())
                .imageFileId(company.getImageFileId())
                .adminUserId(company.getAdminUserId())
                .verificationStatus(company.getVerificationStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}

