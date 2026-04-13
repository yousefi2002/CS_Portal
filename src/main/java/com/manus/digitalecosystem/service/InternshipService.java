package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateInternshipRequest;
import com.manus.digitalecosystem.dto.request.UpdateInternshipRequest;
import com.manus.digitalecosystem.dto.response.InternshipResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface InternshipService {
    InternshipResponse createInternship(CreateInternshipRequest request);

    InternshipResponse updateInternship(String id, UpdateInternshipRequest request);

    void deleteInternship(String id);

    InternshipResponse getInternshipById(String id);

    PagedResponse<InternshipResponse> searchInternships(String q, String companyId, Pageable pageable);
}

