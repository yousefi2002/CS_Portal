package com.manus.digitalecosystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manus.digitalecosystem.dto.request.CreateAchievementRequest;
import com.manus.digitalecosystem.dto.request.DeleteAchievementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAchievementRequest;
import com.manus.digitalecosystem.dto.response.AchievementResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.service.AchievementService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/achievements"})
public class AchievementController {

    private final AchievementService achievementService;
    private final ApiResponseFactory apiResponseFactory;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public AchievementController(AchievementService achievementService,
                                 ApiResponseFactory apiResponseFactory,
                                 ObjectMapper objectMapper,
                                 Validator validator) {
        this.achievementService = achievementService;
        this.apiResponseFactory = apiResponseFactory;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<AchievementResponse>> createAchievement(@RequestPart(value = "data", required = false) String data,
                                                                           @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        CreateAchievementRequest request = parseRequest(data, CreateAchievementRequest.class, "error.achievement.data.required");
        return apiResponseFactory.success(HttpStatus.CREATED, "success.achievement.created",
                achievementService.createAchievement(request, images));
    }

    @PatchMapping("/{achievementId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<AchievementResponse>> updateAchievementData(@PathVariable String achievementId,
                                                                               @Valid @RequestBody UpdateAchievementRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.updated",
                achievementService.updateAchievementData(achievementId, request));
    }

    @PatchMapping(value = "/{achievementId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<AchievementResponse>> updateAchievementImages(@PathVariable String achievementId,
                                                                                 @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.updated",
                achievementService.updateAchievementImages(achievementId, images));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<AchievementResponse>>> getAllAchievements() {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.list", achievementService.getAllAchievements());
    }

    @GetMapping(params = "universityId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<AchievementResponse>>> getAchievementsByUniversity(@RequestParam String universityId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.list",
                achievementService.getAchievementsByUniversity(universityId));
    }

    @GetMapping(params = "departmentId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<AchievementResponse>>> getAchievementsByDepartment(@RequestParam String departmentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.list",
                achievementService.getAchievementsByDepartment(departmentId));
    }

    @GetMapping(params = "companyId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<AchievementResponse>>> getAchievementsByCompany(@RequestParam String companyId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.list",
                achievementService.getAchievementsByCompany(companyId));
    }

    @GetMapping(params = "studentId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<AchievementResponse>>> getAchievementsByStudent(@RequestParam String studentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.list",
                achievementService.getAchievementsByStudent(studentId));
    }

    @GetMapping("/{achievementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<AchievementResponse>> getAchievementById(@PathVariable String achievementId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.fetched",
                achievementService.getAchievementById(achievementId));
    }

    @DeleteMapping("/{achievementId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteAchievement(@PathVariable String achievementId) {
        achievementService.deleteAchievement(achievementId);
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.deleted", null);
    }

    @PatchMapping("/{achievementId}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Response<AchievementResponse>> softDeleteAchievement(@PathVariable String achievementId,
                                                                              @Valid @RequestBody DeleteAchievementRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.deleted",
                achievementService.softDeleteAchievement(achievementId, request));
    }

        @PostMapping("/{achievementId}/students/{studentId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Response<AchievementResponse>> assignStudent(@PathVariable String achievementId,
                                           @PathVariable String studentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.updated",
            achievementService.assignStudentToAchievement(achievementId, studentId));
        }

        @DeleteMapping("/{achievementId}/students/{studentId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Response<AchievementResponse>> removeStudent(@PathVariable String achievementId,
                                           @PathVariable String studentId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.achievement.updated",
            achievementService.removeStudentFromAchievement(achievementId, studentId));
        }

    private <T> T parseRequest(String data, Class<T> requestType, String errorMessageKey) {
        if (data == null || data.isBlank()) {
            throw new BadRequestException(errorMessageKey);
        }

        try {
            T request = objectMapper.readValue(data, requestType);
            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new BadRequestException("error.validation.failed");
            }
            return request;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("error.validation.failed");
        }
    }
}