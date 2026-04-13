package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.University;
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
public class UniversityResponse {
    private String id;
    private String name;
    private String description;
    private String address;
    private String website;
    private String phone;
    private String email;
    private String imageFileId;
    private String adminUserId;
    private VerificationStatus verificationStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public static UniversityResponse fromUniversity(University university) {
        return UniversityResponse.builder()
                .id(university.getId())
                .name(university.getName())
                .description(university.getDescription())
                .address(university.getAddress())
                .website(university.getWebsite())
                .phone(university.getPhone())
                .email(university.getEmail())
                .imageFileId(university.getImageFileId())
                .adminUserId(university.getAdminUserId())
                .verificationStatus(university.getVerificationStatus())
                .createdAt(university.getCreatedAt())
                .updatedAt(university.getUpdatedAt())
                .build();
    }
}

