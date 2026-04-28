package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.model.enums.UniversityVisibility;
import org.springframework.data.domain.Page;

public interface UniversityService {
    UniversityResponse createUniversity(CreateUniversityProfileRequest request);

    UniversityResponse updateUniversity(String universityId, UpdateUniversityProfileRequest request);

    Page<UniversityResponse> getAllUniversities(int page, int size);

    UniversityResponse getUniversityById(String universityId);

    void deleteUniversity(String universityId);

    Page<UniversityResponse> searchUniversities(String search,
                                                UniversityVisibility visibility,
                                                String rank,
                                                int page,
                                                int size);
}