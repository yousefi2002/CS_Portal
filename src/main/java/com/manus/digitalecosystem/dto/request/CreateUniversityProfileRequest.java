package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.UniversityVisibility;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUniversityProfileRequest {

    @NotNull(message = "{validation.university.name.required}")
    private LocalizedText name;

    @NotNull(message = "{validation.university.admin_user_id.required}")
    private String adminUserId;

    private LocalizedText description;

    private LocalizedText address;

    @NotNull(message = "{validation.university.visibility.required}")
    private UniversityVisibility visibility;

    private String website;

    private String phone;

    @Email(message = "{validation.email.invalid}")
    private String email;

    private String imageFileId;
}

