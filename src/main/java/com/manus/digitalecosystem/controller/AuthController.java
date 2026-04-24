package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.ForgotPasswordRequest;
import com.manus.digitalecosystem.dto.request.LoginRequest;
import com.manus.digitalecosystem.dto.request.RefreshTokenRequest;
import com.manus.digitalecosystem.dto.request.ResetPasswordRequest;
import com.manus.digitalecosystem.dto.response.AuthLoginResponse;
import com.manus.digitalecosystem.dto.response.ForgotPasswordResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.service.AuthService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ApiResponseFactory apiResponseFactory;

    public AuthController(AuthService authService, ApiResponseFactory apiResponseFactory) {
        this.authService = authService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/login")
    public ResponseEntity<Response<AuthLoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.auth.login", authService.login(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<Object>> logout() {
        authService.logout();
        return apiResponseFactory.success(HttpStatus.OK, "success.auth.logout", null);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Response<AuthLoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.auth.login", authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.auth.forgot_password", authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return apiResponseFactory.success(HttpStatus.OK, "success.auth.reset_password", null);
    }
}

