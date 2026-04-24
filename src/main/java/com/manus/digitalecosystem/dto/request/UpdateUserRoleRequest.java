package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotNull(message = "{validation.user.role.required}")
    private Role role;
}
