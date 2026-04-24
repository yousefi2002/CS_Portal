package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateUserRequest;
import com.manus.digitalecosystem.dto.request.UpdateUserRoleRequest;
import com.manus.digitalecosystem.dto.request.UpdateUserStatusRequest;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.dto.response.UserResponse;
import com.manus.digitalecosystem.service.UserService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
public class UserController {

    private final UserService userService;
    private final ApiResponseFactory apiResponseFactory;

    public UserController(UserService userService, ApiResponseFactory apiResponseFactory) {
        this.userService = userService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and !hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return apiResponseFactory.success(HttpStatus.CREATED, "success.user.created", userService.createUser(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<UserResponse>>> getAllUsers() {
        return apiResponseFactory.success(HttpStatus.OK, "success.user.list", userService.getAll());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<UserResponse>> getCurrentUser() {
        return apiResponseFactory.success(HttpStatus.OK, "success.user.me", userService.getCurrentUser());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<UserResponse>> getUserById(@PathVariable String userId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.user.fetched", userService.getById(userId));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<UserResponse>> updateStatus(@PathVariable String userId,
                                                               @Valid @RequestBody UpdateUserStatusRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.user.status_updated",
                userService.updateStatus(userId, request.getStatus()));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("isAuthenticated() and !hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<UserResponse>> updateRole(@PathVariable String userId,
                                                             @Valid @RequestBody UpdateUserRoleRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.user.role_updated",
                userService.updateRole(userId, request.getRole()));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return apiResponseFactory.success(HttpStatus.OK, "success.user.deleted", null);
    }
}

