package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.AnnouncementCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAnnouncementRequest {

    @NotNull(message = "{validation.announcement.category.required}")
    private AnnouncementCategory category;

    @NotNull(message = "{validation.announcement.title.required}")
    private LocalizedText title;

    @NotNull(message = "{validation.announcement.content.required}")
    private LocalizedText content;

    private String universityId;

    private String departmentId;
}

