package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.DeleteAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAnnouncementRequest;
import com.manus.digitalecosystem.dto.response.AnnouncementResponse;

import java.util.List;

public interface AnnouncementService {

    AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request);

    AnnouncementResponse updateAnnouncement(String announcementId, UpdateAnnouncementRequest request);

    AnnouncementResponse softDeleteAnnouncement(String announcementId, DeleteAnnouncementRequest request);

    void deleteAnnouncement(String announcementId);

    List<AnnouncementResponse> searchAnnouncements(String query, String universityId, String departmentId);

    List<AnnouncementResponse> getAllAnnouncements();

    AnnouncementResponse getAnnouncementById(String announcementId);

    List<AnnouncementResponse> getAnnouncementsByUniversity(String universityId);

    List<AnnouncementResponse> getAnnouncementsByDepartment(String departmentId);
}
