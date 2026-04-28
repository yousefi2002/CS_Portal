package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUniversityProfileRequest {

    @NotNull(message = "{validation.university.name.required}")
    private LocalizedText name;

    private LocalizedText description;

    private LocalizedText address;

    private String website;

    private String phone;

    @Email(message = "{validation.email.invalid}")
    private String email;

    private String imageFileId;
}

