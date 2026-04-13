package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "{validation.user.status.required}")
    private User.Status status;
}

