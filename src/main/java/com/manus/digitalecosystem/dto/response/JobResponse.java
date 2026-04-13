package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Job;
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
public class JobResponse {
    private String id;
    private String companyId;
    private String title;
    private String description;
    private String requirements;
    private String location;
    private List<String> requiredSkills;
    private Instant createdAt;
    private Instant updatedAt;

    public static JobResponse fromJob(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .location(job.getLocation())
                .requiredSkills(job.getRequiredSkills())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}

