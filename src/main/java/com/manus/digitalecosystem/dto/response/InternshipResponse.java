package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Internship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternshipResponse {
    private String id;
    private String companyId;
    private String title;
    private String description;
    private String requirements;
    private String roadmap;
    private String duration;
    private List<String> requiredSkills;
    private Instant createdAt;
    private Instant updatedAt;

    public static InternshipResponse fromInternship(Internship internship) {
        return InternshipResponse.builder()
                .id(internship.getId())
                .companyId(internship.getCompanyId())
                .title(internship.getTitle())
                .description(internship.getDescription())
                .requirements(internship.getRequirements())
                .roadmap(internship.getRoadmap())
                .duration(internship.getDuration())
                .requiredSkills(internship.getRequiredSkills())
                .createdAt(internship.getCreatedAt())
                .updatedAt(internship.getUpdatedAt())
                .build();
    }
}

