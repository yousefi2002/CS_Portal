package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Announcement;
import com.manus.digitalecosystem.model.enums.AnnouncementCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {
    private String id;
    private String universityId;
    private String departmentId;
    private AnnouncementCategory category;
    private String title;
    private String content;
    private String createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public static AnnouncementResponse fromAnnouncement(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .universityId(announcement.getUniversityId())
                .departmentId(announcement.getDepartmentId())
                .category(announcement.getCategory())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .createdByUserId(announcement.getCreatedByUserId())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}

