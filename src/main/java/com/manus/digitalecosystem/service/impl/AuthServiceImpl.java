package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.ForgotPasswordRequest;
import com.manus.digitalecosystem.dto.request.LoginRequest;
import com.manus.digitalecosystem.dto.request.ResetPasswordRequest;
import com.manus.digitalecosystem.dto.response.AuthLoginResponse;
import com.manus.digitalecosystem.dto.response.ForgotPasswordResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.PasswordResetToken;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.repository.PasswordResetTokenRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.security.JwtUtils;
import com.manus.digitalecosystem.service.AuthService;
import com.manus.digitalecosystem.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${manus.app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils,
                           UserRepository userRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthLoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("error.user.email.not_found", request.getEmail()));

        if (user.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("error.user.inactive");
        }

        return AuthLoginResponse.builder()
                .token(jwtUtils.generateJwtToken(authentication))
                .tokenType("Bearer")
                .expiresInSeconds(jwtExpirationMs / 1000)
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("error.user.email.not_found", request.getEmail()));

        String rawToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(30 * 60);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(hashToken(rawToken))
                .userId(user.getId())
                .expiresAt(expiresAt)
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        return ForgotPasswordResponse.builder()
                .resetToken(rawToken)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(() -> new BadRequestException("error.reset.token.invalid"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("error.reset.token.used");
        }
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("error.reset.token.expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found", resetToken.getUserId()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}

