package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUniversityProfileRequest {

    @NotBlank(message = "{validation.university.name.required}")
    private String name;

    private String description;

    private String address;

    private String website;

    private String phone;

    @Email(message = "{validation.email.invalid}")
    private String email;

    private String imageFileId;
}

