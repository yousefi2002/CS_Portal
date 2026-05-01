package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.AchievementContributor;
import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AchievementResponse {
    private String id;
    private String universityId;
    private String departmentId;
    private String companyId;
    private String type;
    private LocalizedText title;
    private LocalizedText description;
    private String link;
    private List<String> imageUrls;
    private List<AchievementContributor> contributors;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}