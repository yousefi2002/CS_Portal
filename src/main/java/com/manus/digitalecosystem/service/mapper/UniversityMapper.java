package com.manus.digitalecosystem.service.mapper;

import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.model.University;

public final class UniversityMapper {
    private UniversityMapper() {
    }

    public static UniversityResponse toResponse(University university) {
        return UniversityResponse.builder()
                .id(university.getId())
                .name(university.getName())
                .description(university.getDescription())
                .address(university.getAddress())
                .visibility(university.getVisibility())
                .website(university.getWebsite())
                .phone(university.getPhone())
                .email(university.getEmail())
                .imageFileIds(university.getImageFileIds())
                .adminUserId(university.getAdminUserId())
                .verificationStatus(university.getVerificationStatus())
                .createdAt(university.getCreatedAt())
                .updatedAt(university.getUpdatedAt())
                .build();
    }
}