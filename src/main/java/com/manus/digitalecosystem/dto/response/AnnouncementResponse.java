package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.AnnouncementCategory;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AnnouncementResponse {

    private String id;

    private String universityId;

    private String departmentId;

    private AnnouncementCategory category;

    private LocalizedText title;

    private LocalizedText content;

    private String createdByUserId;

    private boolean deleted;

    private Instant createdAt;

    private Instant updatedAt;
}
