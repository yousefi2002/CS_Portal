package com.manus.digitalecosystem.service.mapper;

import com.manus.digitalecosystem.model.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static com.manus.digitalecosystem.dto.response.UserResponse toResponse(User user) {
        return com.manus.digitalecosystem.dto.response.UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
