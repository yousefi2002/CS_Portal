package com.manus.digitalecosystem.service;

public interface PasswordResetService {
    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);
}

