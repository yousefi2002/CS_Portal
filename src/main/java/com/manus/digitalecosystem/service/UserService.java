package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateUserRequest;
import com.manus.digitalecosystem.dto.response.UserResponse;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse getById(String userId);

    UserResponse getCurrentUser();

    List<UserResponse> getAll();

    UserResponse updateStatus(String userId, Status status);

    UserResponse updateRole(String userId, Role role);

    void deleteUser(String userId);
}

