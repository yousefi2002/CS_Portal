package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTaskCompletionRequest {
    @NotNull(message = "{validation.task.completed.required}")
    private Boolean completed;
}

