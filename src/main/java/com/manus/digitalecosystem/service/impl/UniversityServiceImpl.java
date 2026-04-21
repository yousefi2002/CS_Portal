package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.UniversityService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;

    public UniversityServiceImpl(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @Override
    public UniversityResponse createMyUniversity(CreateUniversityProfileRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (universityRepository.findByAdminUserId(currentUserId).isPresent()) {
            throw new DuplicateResourceException("error.university.profile.exists");
        }

        University university = University.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageFileId(request.getImageFileId())
                .adminUserId(currentUserId)
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        return UniversityResponse.fromUniversity(universityRepository.save(university));
    }

    @Override
    public UniversityResponse getMyUniversity() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        University university = universityRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
        return UniversityResponse.fromUniversity(university);
    }

    @Override
    public UniversityResponse updateMyUniversity(UpdateUniversityProfileRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        University university = universityRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));

        university.setName(request.getName());
        university.setDescription(request.getDescription());
        university.setAddress(request.getAddress());
        university.setWebsite(request.getWebsite());
        university.setPhone(request.getPhone());
        university.setEmail(request.getEmail());
        university.setImageFileId(request.getImageFileId());

        return UniversityResponse.fromUniversity(universityRepository.save(university));
    }

    @Override
    public PagedResponse<UniversityResponse> searchUniversities(String q, VerificationStatus status, Pageable pageable) {
        boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
        VerificationStatus effectiveStatus = isSuperAdmin ? status : VerificationStatus.APPROVED;

        Page<University> page;
        if (q == null || q.isBlank()) {
            if (effectiveStatus == null) {
                page = universityRepository.findAll(pageable);
            } else {
                page = universityRepository.findByVerificationStatus(effectiveStatus, pageable);
            }
        } else {
            if (effectiveStatus == null) {
                page = universityRepository.findByNameContainingIgnoreCase(q, pageable);
            } else {
                page = universityRepository.findByVerificationStatusAndNameContainingIgnoreCase(effectiveStatus, q, pageable);
            }
        }

        return PagedResponse.fromPage(page.map(UniversityResponse::fromUniversity));
    }

    @Override
    public UniversityResponse getUniversityById(String id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", id));

        boolean canViewUnverified = SecurityUtils.hasRole("SUPER_ADMIN")
                || SecurityUtils.getCurrentUserId().equals(university.getAdminUserId());
        if (!canViewUnverified && university.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new AccessDeniedException("Forbidden");
        }

        return UniversityResponse.fromUniversity(university);
    }

    @Override
    public UniversityResponse updateVerificationStatus(String id, VerificationStatus verificationStatus) {
        if (!SecurityUtils.hasRole("SUPER_ADMIN")) {
            throw new AccessDeniedException("Forbidden");
        }

        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", id));

        university.setVerificationStatus(verificationStatus);
        return UniversityResponse.fromUniversity(universityRepository.save(university));
    }

    @Override
    public void deleteUniversity(String id) {
        if (!universityRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.university.not_found", id);
        }
        universityRepository.deleteById(id);
    }
}

