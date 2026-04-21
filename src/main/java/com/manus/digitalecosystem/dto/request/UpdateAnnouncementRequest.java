package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.enums.AnnouncementCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAnnouncementRequest {

    @NotNull(message = "{validation.announcement.category.required}")
    private AnnouncementCategory category;

    @NotBlank(message = "{validation.announcement.title.required}")
    private String title;

    @NotBlank(message = "{validation.announcement.content.required}")
    private String content;

    private String universityId;

    private String departmentId;
}

