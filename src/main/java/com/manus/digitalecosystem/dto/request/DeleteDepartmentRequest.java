package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteDepartmentRequest {

    @NotNull(message = "{validation.department.deleted.required}")
    private Boolean deleted;
}