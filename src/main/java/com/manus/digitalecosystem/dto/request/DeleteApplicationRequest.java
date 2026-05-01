package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteApplicationRequest {

    @NotNull(message = "{validation.application.deleted.required}")
    private Boolean deleted;
}
