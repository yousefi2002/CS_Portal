package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVerificationStatusRequest {

    @NotNull(message = "{validation.verification_status.required}")
    private VerificationStatus verificationStatus;
}

