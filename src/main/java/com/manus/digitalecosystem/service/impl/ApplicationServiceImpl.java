package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateApplicationRequest;
import com.manus.digitalecosystem.dto.request.UpdateApplicationStatusRequest;
import com.manus.digitalecosystem.dto.response.ApplicationResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.*;
import com.manus.digitalecosystem.repository.*;
import com.manus.digitalecosystem.service.ApplicationService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final InternshipRepository internshipRepository;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            StudentRepository studentRepository,
            CompanyRepository companyRepository,
            JobRepository jobRepository,
            InternshipRepository internshipRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.internshipRepository = internshipRepository;
    }

    @Override
    public ApplicationResponse apply(CreateApplicationRequest request) {
        Student student = getMyStudentOrThrow();
        if (student.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BadRequestException("error.student.not_verified");
        }

        if (applicationRepository.existsByStudentIdAndOpportunityTypeAndOpportunityId(
                student.getId(),
                request.getOpportunityType(),
                request.getOpportunityId()
        )) {
            throw new DuplicateResourceException("error.application.duplicate");
        }

        String companyId = getCompanyIdForOpportunity(request.getOpportunityType(), request.getOpportunityId());

        Application application = Application.builder()
                .studentId(student.getId())
                .companyId(companyId)
                .opportunityType(request.getOpportunityType())
                .opportunityId(request.getOpportunityId())
                .status(ApplicationStatus.SUBMITTED)
                .note(request.getNote())
                .build();

        return ApplicationResponse.fromApplication(applicationRepository.save(application));
    }

    @Override
    public PagedResponse<ApplicationResponse> getMyApplications(Pageable pageable) {
        Student student = getMyStudentOrThrow();
        Page<Application> page = applicationRepository.findByStudentId(student.getId(), pageable);
        return PagedResponse.fromPage(page.map(ApplicationResponse::fromApplication));
    }

    @Override
    public PagedResponse<ApplicationResponse> getCompanyApplications(OpportunityType opportunityType, String opportunityId, Pageable pageable) {
        String companyId = getMyCompanyOrThrow().getId();

        Page<Application> page;
        if (opportunityType != null && opportunityId != null && !opportunityId.isBlank()) {
            page = applicationRepository.findByCompanyIdAndOpportunityTypeAndOpportunityId(companyId, opportunityType, opportunityId, pageable);
        } else if (opportunityType != null) {
            page = applicationRepository.findByCompanyIdAndOpportunityType(companyId, opportunityType, pageable);
        } else {
            page = applicationRepository.findByCompanyId(companyId, pageable);
        }

        return PagedResponse.fromPage(page.map(ApplicationResponse::fromApplication));
    }

    @Override
    public ApplicationResponse updateStatus(String id, UpdateApplicationStatusRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.application.not_found", id));

        if (!SecurityUtils.hasRole("SUPER_ADMIN")) {
            String companyId = getMyCompanyOrThrow().getId();
            if (!companyId.equals(application.getCompanyId())) {
                throw new AccessDeniedException("Forbidden");
            }
        }

        application.setStatus(request.getStatus());
        return ApplicationResponse.fromApplication(applicationRepository.save(application));
    }

    private Student getMyStudentOrThrow() {
        return studentRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));
    }

    private Company getMyCompanyOrThrow() {
        return companyRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("error.company.profile.not_found"));
    }

    private String getCompanyIdForOpportunity(OpportunityType opportunityType, String opportunityId) {
        if (opportunityType == OpportunityType.JOB) {
            Job job = jobRepository.findById(opportunityId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.job.not_found", opportunityId));
            return job.getCompanyId();
        }
        if (opportunityType == OpportunityType.INTERNSHIP) {
            Internship internship = internshipRepository.findById(opportunityId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.internship.not_found", opportunityId));
            return internship.getCompanyId();
        }
        throw new BadRequestException("error.application.opportunity_type.invalid");
    }
}

