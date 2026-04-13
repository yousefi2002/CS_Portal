package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateAchievementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAchievementRequest;
import com.manus.digitalecosystem.dto.response.AchievementResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.service.AchievementService;
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
@RequestMapping("/api/achievements")
@Tag(name = "Achievements", description = "Student achievements (academic record)")
@SecurityRequirement(name = "bearerAuth")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Create an achievement for a student (University/Department Admin)")
    public ResponseEntity<AchievementResponse> create(@Valid @RequestBody CreateAchievementRequest request) {
        AchievementResponse response = achievementService.createAchievement(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Update an achievement")
    public ResponseEntity<AchievementResponse> update(@PathVariable String id, @Valid @RequestBody UpdateAchievementRequest request) {
        return ResponseEntity.ok(achievementService.updateAchievement(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Delete an achievement")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN','STUDENT')")
    @Operation(summary = "Get achievement by id")
    public ResponseEntity<AchievementResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(achievementService.getAchievementById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN','STUDENT')")
    @Operation(summary = "Search achievements (pagination)")
    public ResponseEntity<PagedResponse<AchievementResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String universityId,
            @RequestParam(required = false) String departmentId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(achievementService.searchAchievements(q, studentId, universityId, departmentId, pageable));
    }
}

