package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteAchievementRequest {

    @NotNull(message = "{validation.achievement.deleted.required}")
    private Boolean deleted;
}