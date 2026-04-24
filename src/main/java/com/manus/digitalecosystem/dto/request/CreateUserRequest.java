package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "{validation.user.full_name.required}")
    private String fullName;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, message = "{validation.password.size}")
    private String password;

    @NotNull(message = "{validation.role.required}")
    private Role role;

    private Status status;
}
