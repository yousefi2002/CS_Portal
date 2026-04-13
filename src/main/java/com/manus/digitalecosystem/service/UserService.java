package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateUserRequest;
import com.manus.digitalecosystem.dto.response.UserResponse;
import com.manus.digitalecosystem.model.User;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(String id);
    List<UserResponse> getAllUsers();
    void deleteUser(String id);

    UserResponse updateUserStatus(String id, User.Status status);
}
