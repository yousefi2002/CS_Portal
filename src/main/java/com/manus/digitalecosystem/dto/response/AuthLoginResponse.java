package com.manus.digitalecosystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthLoginResponse {
    private String token;
    private String refreshToken;
    private String tokenType;
    private Integer expiresInSeconds;
    private Integer refreshTokenExpiresInSeconds;
    private UserResponse user;
}

