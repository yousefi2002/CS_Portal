package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private Role role;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

