package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.DeleteAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAnnouncementRequest;
import com.manus.digitalecosystem.dto.response.AnnouncementResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.service.AnnouncementService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/announcements"})
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final ApiResponseFactory apiResponseFactory;

    public AnnouncementController(AnnouncementService announcementService,
                                  ApiResponseFactory apiResponseFactory) {
        this.announcementService = announcementService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<AnnouncementResponse>> createAnnouncement(@Valid @RequestBody CreateAnnouncementRequest request) {
        return apiResponseFactory.success(HttpStatus.CREATED, "success.announcement.created", announcementService.createAnnouncement(request));
    }

    @PatchMapping("/{announcementId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<AnnouncementResponse>> updateAnnouncement(@PathVariable String announcementId,
                                                                             @Valid @RequestBody UpdateAnnouncementRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.updated", announcementService.updateAnnouncement(announcementId, request));
    }

    @PatchMapping("/{announcementId}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<AnnouncementResponse>> softDeleteAnnouncement(@PathVariable String announcementId,
                                                                                 @Valid @RequestBody DeleteAnnouncementRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.deleted", announcementService.softDeleteAnnouncement(announcementId, request));
    }

    @DeleteMapping("/{announcementId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteAnnouncement(@PathVariable String announcementId) {
        announcementService.deleteAnnouncement(announcementId);
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.deleted", null);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<List<AnnouncementResponse>>> searchAnnouncements(@RequestParam(required = false) String q,
                                                                                    @RequestParam(required = false) String universityId,
                                                                                    @RequestParam(required = false) String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.search",
                announcementService.searchAnnouncements(q, universityId, departmentId));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<AnnouncementResponse>>> getAllAnnouncements() {
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.list", announcementService.getAllAnnouncements());
    }

    @GetMapping("/{announcementId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<AnnouncementResponse>> getAnnouncementById(@PathVariable String announcementId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.fetched", announcementService.getAnnouncementById(announcementId));
    }

    @GetMapping("/university/{universityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<List<AnnouncementResponse>>> getAnnouncementsByUniversity(@PathVariable String universityId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.list",
                announcementService.getAnnouncementsByUniversity(universityId));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Response<List<AnnouncementResponse>>> getAnnouncementsByDepartment(@PathVariable String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.announcement.list",
                announcementService.getAnnouncementsByDepartment(departmentId));
    }
}
