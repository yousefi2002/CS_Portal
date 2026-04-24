package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "{validation.user.status.required}")
    private Status status;
}
