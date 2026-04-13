package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateAchievementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAchievementRequest;
import com.manus.digitalecosystem.dto.response.AchievementResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface AchievementService {
    AchievementResponse createAchievement(CreateAchievementRequest request);

    AchievementResponse updateAchievement(String id, UpdateAchievementRequest request);

    void deleteAchievement(String id);

    AchievementResponse getAchievementById(String id);

    PagedResponse<AchievementResponse> searchAchievements(String q, String studentId, String universityId, String departmentId, Pageable pageable);
}

