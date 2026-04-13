package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Achievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    private String id;
    private String studentId;
    private String universityId;
    private String departmentId;
    private String title;
    private String description;
    private Instant achievedAt;
    private String createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public static AchievementResponse fromAchievement(Achievement achievement) {
        return AchievementResponse.builder()
                .id(achievement.getId())
                .studentId(achievement.getStudentId())
                .universityId(achievement.getUniversityId())
                .departmentId(achievement.getDepartmentId())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .achievedAt(achievement.getAchievedAt())
                .createdByUserId(achievement.getCreatedByUserId())
                .createdAt(achievement.getCreatedAt())
                .updatedAt(achievement.getUpdatedAt())
                .build();
    }
}

