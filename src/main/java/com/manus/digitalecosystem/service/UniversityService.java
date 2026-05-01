package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.model.enums.UniversityVisibility;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UniversityService {
    UniversityResponse createUniversity(CreateUniversityProfileRequest request, List<MultipartFile> images);

    UniversityResponse updateUniversity(String universityId, UpdateUniversityProfileRequest request);

    UniversityResponse updateUniversityImages(String universityId, List<MultipartFile> images);

    Page<UniversityResponse> getAllUniversities(int page, int size);

    UniversityResponse getUniversityById(String universityId);

    void deleteUniversity(String universityId);

    Page<UniversityResponse> searchUniversities(String search,
                                                UniversityVisibility visibility,
                                                String rank,
                                                int page,
                                                int size);

    java.util.List<com.manus.digitalecosystem.dto.response.TopStudentResponse> getTopStudents(String universityId, int limit);
}