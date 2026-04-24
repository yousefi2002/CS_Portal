package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.ForgotPasswordRequest;
import com.manus.digitalecosystem.dto.request.LoginRequest;
import com.manus.digitalecosystem.dto.request.ResetPasswordRequest;
import com.manus.digitalecosystem.dto.response.AuthLoginResponse;
import com.manus.digitalecosystem.dto.response.ForgotPasswordResponse;

public interface AuthService {
    AuthLoginResponse login(LoginRequest request);

    void logout();

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}

