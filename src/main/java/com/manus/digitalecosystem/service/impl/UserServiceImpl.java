package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateUserRequest;
import com.manus.digitalecosystem.dto.response.UserResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.service.UserService;
import com.manus.digitalecosystem.service.mapper.UserMapper;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("error.user.email.exists", request.getEmail());
        }

        validateLocalizedText(request.getFullName());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(request.getStatus() == null ? Status.ACTIVE : request.getStatus())
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getById(String userId) {
        return UserMapper.toResponse(findUserById(userId));
    }

    @Override
    public UserResponse getCurrentUser() {
        return UserMapper.toResponse(findUserById(SecurityUtils.getCurrentUserId()));
    }

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateStatus(String userId, Status status) {
        User user = findUserById(userId);
        user.setStatus(status);
        return UserMapper.toResponse(userRepository.save(user));
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found", userId));
    }

    private void validateLocalizedText(LocalizedText localizedText) {
        if (localizedText == null
                || isBlank(localizedText.getEn())
                || isBlank(localizedText.getFa())
                || isBlank(localizedText.getPs())) {
            throw new BadRequestException("error.user.translation.required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

