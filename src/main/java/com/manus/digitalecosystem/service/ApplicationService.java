package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateApplicationRequest;
import com.manus.digitalecosystem.dto.request.UpdateApplicationStatusRequest;
import com.manus.digitalecosystem.dto.response.ApplicationResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.model.OpportunityType;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    ApplicationResponse apply(CreateApplicationRequest request);

    PagedResponse<ApplicationResponse> getMyApplications(Pageable pageable);

    PagedResponse<ApplicationResponse> getCompanyApplications(OpportunityType opportunityType, String opportunityId, Pageable pageable);

    ApplicationResponse updateStatus(String id, UpdateApplicationStatusRequest request);
}

