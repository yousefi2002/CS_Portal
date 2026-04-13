package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.response.CompanyResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.VerificationStatus;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.service.CompanyService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyResponse createMyCompany(CreateCompanyProfileRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (companyRepository.findByAdminUserId(currentUserId).isPresent()) {
            throw new DuplicateResourceException("error.company.profile.exists");
        }

        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .developmentType(request.getDevelopmentType())
                .achievements(request.getAchievements())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageFileId(request.getImageFileId())
                .adminUserId(currentUserId)
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        return CompanyResponse.fromCompany(companyRepository.save(company));
    }

    @Override
    public CompanyResponse getMyCompany() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Company company = companyRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.profile.not_found"));
        return CompanyResponse.fromCompany(company);
    }

    @Override
    public CompanyResponse updateMyCompany(UpdateCompanyProfileRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Company company = companyRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.profile.not_found"));

        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setDevelopmentType(request.getDevelopmentType());
        company.setAchievements(request.getAchievements());
        company.setWebsite(request.getWebsite());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setImageFileId(request.getImageFileId());

        return CompanyResponse.fromCompany(companyRepository.save(company));
    }

    @Override
    public PagedResponse<CompanyResponse> searchCompanies(String q, VerificationStatus status, Pageable pageable) {
        boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
        VerificationStatus effectiveStatus = isSuperAdmin ? status : VerificationStatus.APPROVED;

        Page<Company> page;
        if (q == null || q.isBlank()) {
            if (effectiveStatus == null) {
                page = companyRepository.findAll(pageable);
            } else {
                page = companyRepository.findByVerificationStatus(effectiveStatus, pageable);
            }
        } else {
            if (effectiveStatus == null) {
                page = companyRepository.findByNameContainingIgnoreCase(q, pageable);
            } else {
                page = companyRepository.findByVerificationStatusAndNameContainingIgnoreCase(effectiveStatus, q, pageable);
            }
        }

        return PagedResponse.fromPage(page.map(CompanyResponse::fromCompany));
    }

    @Override
    public CompanyResponse getCompanyById(String id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.not_found", id));

        boolean canViewUnverified = SecurityUtils.hasRole("SUPER_ADMIN")
                || SecurityUtils.getCurrentUserId().equals(company.getAdminUserId());
        if (!canViewUnverified && company.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new AccessDeniedException("Forbidden");
        }

        return CompanyResponse.fromCompany(company);
    }

    @Override
    public CompanyResponse updateVerificationStatus(String id, VerificationStatus verificationStatus) {
        if (!SecurityUtils.hasRole("SUPER_ADMIN")) {
            throw new AccessDeniedException("Forbidden");
        }

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.not_found", id));

        company.setVerificationStatus(verificationStatus);
        return CompanyResponse.fromCompany(companyRepository.save(company));
    }

    @Override
    public void deleteCompany(String id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.company.not_found", id);
        }
        companyRepository.deleteById(id);
    }
}

