package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateAchievementRequest {

    @NotBlank(message = "{validation.achievement.title.required}")
    private String title;

    private String description;

    private Instant achievedAt;
}

