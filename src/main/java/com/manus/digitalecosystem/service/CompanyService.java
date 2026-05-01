package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.response.CompanyResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyProfileRequest request, List<MultipartFile> images);

    CompanyResponse updateCompanyData(String companyId, UpdateCompanyProfileRequest request);

    CompanyResponse updateCompanyImages(String companyId, List<MultipartFile> images);

    List<CompanyResponse> getAllCompanies();

    CompanyResponse getCompanyById(String companyId);

    void deleteCompany(String companyId);

    List<CompanyResponse> searchCompanies(String query);
}
