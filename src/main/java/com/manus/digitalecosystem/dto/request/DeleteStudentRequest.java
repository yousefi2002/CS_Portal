package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteStudentRequest {

    @NotNull(message = "{validation.student.deleted.required}")
    private Boolean deleted;
}
