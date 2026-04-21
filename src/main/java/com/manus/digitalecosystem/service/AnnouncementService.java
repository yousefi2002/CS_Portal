package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAnnouncementRequest;
import com.manus.digitalecosystem.dto.response.AnnouncementResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.model.enums.AnnouncementCategory;
import org.springframework.data.domain.Pageable;

public interface AnnouncementService {
    AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request);

    AnnouncementResponse updateAnnouncement(String id, UpdateAnnouncementRequest request);

    void deleteAnnouncement(String id);

    AnnouncementResponse getAnnouncementById(String id);

    PagedResponse<AnnouncementResponse> searchAnnouncements(
            String q,
            AnnouncementCategory category,
            String universityId,
            String departmentId,
            Pageable pageable
    );
}

