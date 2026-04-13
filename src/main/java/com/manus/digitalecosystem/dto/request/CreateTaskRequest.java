package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "{validation.task.title.required}")
    private String title;

    private String description;

    private Instant dueAt;

    private Boolean assignedToAll;

    private List<String> assignedToUserIds;
}

