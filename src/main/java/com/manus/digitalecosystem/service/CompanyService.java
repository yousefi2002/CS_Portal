package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.response.CompanyResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.model.VerificationStatus;
import org.springframework.data.domain.Pageable;

public interface CompanyService {
    CompanyResponse createMyCompany(CreateCompanyProfileRequest request);

    CompanyResponse getMyCompany();

    CompanyResponse updateMyCompany(UpdateCompanyProfileRequest request);

    PagedResponse<CompanyResponse> searchCompanies(String q, VerificationStatus status, Pageable pageable);

    CompanyResponse getCompanyById(String id);

    CompanyResponse updateVerificationStatus(String id, VerificationStatus verificationStatus);

    void deleteCompany(String id);
}

