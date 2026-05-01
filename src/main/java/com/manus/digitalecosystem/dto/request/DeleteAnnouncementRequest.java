package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteAnnouncementRequest {

    @NotNull(message = "{validation.announcement.deleted.required}")
    private Boolean deleted;
}
