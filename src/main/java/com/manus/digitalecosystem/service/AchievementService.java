package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateAchievementRequest;
import com.manus.digitalecosystem.dto.request.DeleteAchievementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAchievementRequest;
import com.manus.digitalecosystem.dto.response.AchievementResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AchievementService {

    AchievementResponse createAchievement(CreateAchievementRequest request, List<MultipartFile> images);

    AchievementResponse updateAchievementData(String achievementId, UpdateAchievementRequest request);

    AchievementResponse updateAchievementImages(String achievementId, List<MultipartFile> images);

    List<AchievementResponse> getAllAchievements();

    AchievementResponse getAchievementById(String achievementId);

    List<AchievementResponse> getAchievementsByUniversity(String universityId);

    List<AchievementResponse> getAchievementsByDepartment(String departmentId);

    List<AchievementResponse> getAchievementsByCompany(String companyId);

    void deleteAchievement(String achievementId);

    AchievementResponse softDeleteAchievement(String achievementId, DeleteAchievementRequest request);

    AchievementResponse assignStudentToAchievement(String achievementId, String studentId);

    AchievementResponse removeStudentFromAchievement(String achievementId, String studentId);
}