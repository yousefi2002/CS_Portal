package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteInternshipRequest {

    @NotNull(message = "{validation.internship.deleted.required}")
    private Boolean deleted;
}
