package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteJobRequest {

    @NotNull(message = "{validation.job.deleted.required}")
    private Boolean deleted;
}
