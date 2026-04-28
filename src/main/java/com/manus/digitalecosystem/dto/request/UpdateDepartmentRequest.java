package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {

    @NotNull(message = "{validation.department.name.required}")
    private LocalizedText name;

    private LocalizedText description;

    private String imageFileId;

    private String adminUserId;
}

