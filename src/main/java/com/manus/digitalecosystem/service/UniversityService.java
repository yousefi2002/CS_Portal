package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import org.springframework.data.domain.Pageable;

public interface UniversityService {
    UniversityResponse createMyUniversity(CreateUniversityProfileRequest request);

    UniversityResponse getMyUniversity();

    UniversityResponse updateMyUniversity(UpdateUniversityProfileRequest request);

    PagedResponse<UniversityResponse> searchUniversities(String q, VerificationStatus status, Pageable pageable);

    UniversityResponse getUniversityById(String id);

    UniversityResponse updateVerificationStatus(String id, VerificationStatus verificationStatus);

    void deleteUniversity(String id);
}

