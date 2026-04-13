package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateApplicationStatusRequest {

    @NotNull(message = "{validation.application.status.required}")
    private ApplicationStatus status;
}

