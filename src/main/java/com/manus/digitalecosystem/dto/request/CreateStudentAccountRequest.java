package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStudentAccountRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.student.full_name.required}")
    private String fullName;

    private String phone;

    @NotBlank(message = "{validation.student.university_id.required}")
    private String universityId;

    @NotBlank(message = "{validation.student.department_id.required}")
    private String departmentId;
}

