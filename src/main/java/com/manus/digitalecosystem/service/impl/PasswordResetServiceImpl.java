package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.PasswordResetToken;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.repository.PasswordResetTokenRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }

        String token = generateToken();
        String tokenHash = sha256Hex(token);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .userId(userOpt.get().getId())
                .expiresAt(Instant.now().plus(TOKEN_TTL))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        logger.info("Password reset token for {}: {}", email, token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String tokenHash = sha256Hex(token);
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("error.reset.token.invalid"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("error.reset.token.used");
        }

        if (resetToken.getExpiresAt() != null && resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("error.reset.token.expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found", resetToken.getUserId()));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}

