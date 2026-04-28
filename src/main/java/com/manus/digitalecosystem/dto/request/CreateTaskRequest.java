package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CreateTaskRequest {

    @NotNull(message = "{validation.task.title.required}")
    private LocalizedText title;

    private LocalizedText description;

    private Instant dueAt;

    private Boolean assignedToAll;

    private List<String> assignedToUserIds;
}

