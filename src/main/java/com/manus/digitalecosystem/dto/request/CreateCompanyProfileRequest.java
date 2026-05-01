package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCompanyProfileRequest {

    @NotNull(message = "{validation.company.name.required}")
    private LocalizedText name;

    private LocalizedText description;

    private LocalizedText developmentType;

    private LocalizedText achievements;

    private String website;

    private String phone;

    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.company.admin_user_id.required}")
    private String adminUserId;
}

