package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.ForgotPasswordRequest;
import com.manus.digitalecosystem.dto.request.LoginRequest;
import com.manus.digitalecosystem.dto.request.RefreshTokenRequest;
import com.manus.digitalecosystem.dto.request.ResetPasswordRequest;
import com.manus.digitalecosystem.dto.response.AuthLoginResponse;
import com.manus.digitalecosystem.dto.response.ForgotPasswordResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.PasswordResetToken;
import com.manus.digitalecosystem.model.RefreshToken;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.repository.PasswordResetTokenRepository;
import com.manus.digitalecosystem.repository.RefreshTokenRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.security.JwtUtils;
import com.manus.digitalecosystem.service.AuthService;
import com.manus.digitalecosystem.service.mapper.UserMapper;
import com.manus.digitalecosystem.util.SecurityUtils;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${manus.app.jwtExpirationMs:3600000}")
    private long jwtExpirationMs;

    @Value("${manus.app.jwtRefreshExpirationMs:2592000000}")
    private long jwtRefreshExpirationMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils,
                           UserRepository userRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
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

        return issueTokensForUser(user);
    }

    @Override
    public AuthLoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
                .orElseThrow(() -> new UnauthorizedException("error.auth.unauthorized"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("error.auth.unauthorized");
        }

        String userId = refreshToken.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("error.auth.unauthorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("error.auth.unauthorized"));

        if (user.getStatus() != Status.ACTIVE) {
            throw new UnauthorizedException("error.auth.unauthorized");
        }

        return issueTokensForUser(user);
    }

    private AuthLoginResponse issueTokensForUser(User user) {
        String accessToken = jwtUtils.generateJwtTokenFromUsername(user.getEmail());
        String rawRefreshToken = UUID.randomUUID().toString();
        Instant refreshExpiresAt = Instant.now().plusMillis(jwtRefreshExpirationMs);

        refreshTokenRepository.findByUserId(user.getId())
            .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashToken(rawRefreshToken))
                .userId(user.getId())
                .expiresAt(refreshExpiresAt)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthLoginResponse.builder()
                .token(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresInSeconds((int) (jwtExpirationMs / 1000))
                .refreshTokenExpiresInSeconds((int) (jwtRefreshExpirationMs / 1000))
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout() {
        refreshTokenRepository.deleteByUserId(SecurityUtils.getCurrentUserId());
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

