package com.manus.digitalecosystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ForgotPasswordResponse {
    private String resetToken;
    private Instant expiresAt;
}

