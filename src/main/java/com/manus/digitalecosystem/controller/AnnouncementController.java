package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAnnouncementRequest;
import com.manus.digitalecosystem.dto.response.AnnouncementResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.model.enums.AnnouncementCategory;
import com.manus.digitalecosystem.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@Tag(name = "Announcements", description = "Notices, announcements, and events")
@SecurityRequirement(name = "bearerAuth")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Create an announcement/notice/event")
    public ResponseEntity<AnnouncementResponse> create(@Valid @RequestBody CreateAnnouncementRequest request) {
        AnnouncementResponse response = announcementService.createAnnouncement(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Update an announcement/notice/event")
    public ResponseEntity<AnnouncementResponse> update(@PathVariable String id, @Valid @RequestBody UpdateAnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Delete an announcement/notice/event")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by id")
    public ResponseEntity<AnnouncementResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id));
    }

    @GetMapping
    @Operation(summary = "Search announcements (pagination)")
    public ResponseEntity<PagedResponse<AnnouncementResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AnnouncementCategory category,
            @RequestParam(required = false) String universityId,
            @RequestParam(required = false) String departmentId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(announcementService.searchAnnouncements(q, category, universityId, departmentId, pageable));
    }
}

