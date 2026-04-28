package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateAchievementRequest {

    @NotNull(message = "{validation.achievement.student_id.required}")
    private String studentId;

    @NotNull(message = "{validation.achievement.title.required}")
    private LocalizedText title;

    private LocalizedText description;

    private Instant achievedAt;
}

