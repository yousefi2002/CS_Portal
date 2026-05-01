package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateApplicationRequest;
import com.manus.digitalecosystem.dto.request.DeleteApplicationRequest;
import com.manus.digitalecosystem.dto.request.UpdateApplicationRequest;
import com.manus.digitalecosystem.dto.response.ApplyRequirementsResponse;
import com.manus.digitalecosystem.dto.response.ApplicationResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Application;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Internship;
import com.manus.digitalecosystem.model.Job;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.ApplicationStatus;
import com.manus.digitalecosystem.model.enums.JobType;
import com.manus.digitalecosystem.model.enums.OpportunityType;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.ApplicationRepository;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.InternshipRepository;
import com.manus.digitalecosystem.repository.JobRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.service.ApplicationService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  StudentRepository studentRepository,
                                  CompanyRepository companyRepository,
                                  JobRepository jobRepository,
                                  InternshipRepository internshipRepository,
                                  UserRepository userRepository,
                                  MongoTemplate mongoTemplate) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest request) {
        if (!SecurityUtils.hasRole(Role.STUDENT.name())) {
            throw new UnauthorizedException("error.auth.forbidden");
        }

        Student student = findCurrentStudent(request.getStudentId());
        OpportunityContext opportunity = resolveOpportunity(request.getOpportunityType(), request.getOpportunityId());
        validateSubmissionAgainstRequirements(request, opportunity.requirements());

        if (applicationRepository.existsByStudentIdAndOpportunityTypeAndOpportunityId(student.getId(), request.getOpportunityType(), request.getOpportunityId())) {
            throw new DuplicateResourceException("error.application.duplicate");
        }

        Application application = Application.builder()
                .companyId(opportunity.companyId())
                .opportunityId(request.getOpportunityId())
                .studentId(student.getId())
                .opportunityType(request.getOpportunityType())
                .status(ApplicationStatus.SUBMITTED)
                .studentName(student.getFullName())
                .studentImage(student.getImageFileId())
                .studentEmail(student.getEmail())
                .resumeFileId(request.getResumeFileId())
                .portfolioLink(request.getPortfolioLink())
                .coverLetter(request.getCoverLetter() != null ? request.getCoverLetter() : request.getNote())
                .build();

        return toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplication(String applicationId, UpdateApplicationRequest request) {
        Application application = findVisibleApplicationById(applicationId);
        ensureCanManageApplication(application);

        if (request.getResumeFileId() != null) {
            application.setResumeFileId(request.getResumeFileId());
        }
        if (request.getPortfolioLink() != null) {
            application.setPortfolioLink(request.getPortfolioLink());
        }
        if (request.getCoverLetter() != null) {
            application.setCoverLetter(request.getCoverLetter());
        }

        return toResponse(applicationRepository.save(application));
    }

    @Override
    public List<ApplicationResponse> getAllApplications() {
        ensureSuperAdmin();
        return applicationRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ApplicationResponse getApplicationById(String applicationId) {
        Application application = findVisibleApplicationById(applicationId);
        ensureCanViewApplication(application);
        return toResponse(application);
    }

    @Override
    public List<ApplicationResponse> getApplicationsByCompany(String companyId) {
        ensureCanAccessCompanyScope(companyId);
        return applicationRepository.findByCompanyIdAndDeletedFalse(companyId, Pageable.unpaged()).stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ApplicationResponse> getApplicationsByStudent(String studentId) {
        Student student = findStudentById(studentId);
        ensureCanAccessStudentScope(student);
        return applicationRepository.findByStudentIdAndDeletedFalse(student.getId(), Pageable.unpaged()).stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ApplicationResponse softDeleteApplication(String applicationId, DeleteApplicationRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getDeleted())) {
            throw new BadRequestException("error.application.deleted.required");
        }

        Application application = findVisibleApplicationById(applicationId);
        ensureCanManageApplication(application);

        application.setDeleted(true);
        return toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public void deleteApplication(String applicationId) {
        ensureSuperAdmin();
        Application application = findApplicationById(applicationId);
        applicationRepository.delete(application);
    }

    @Override
    public List<ApplicationResponse> searchApplicationsByCompany(String companyId, String query) {
        ensureCanAccessCompanyScope(companyId);
        return searchBase(companyId, query);
    }

    @Override
    public List<ApplicationResponse> searchApplicationsGlobal(String query) {
        ensureSuperAdmin();
        return searchBase(null, query);
    }

    @Override
    public ApplyRequirementsResponse getApplyRequirements(String opportunityType, String opportunityId) {
        OpportunityType type = parseOpportunityType(opportunityType);
        OpportunityContext opportunity = resolveOpportunity(type, opportunityId);
        return ApplyRequirementsResponse.builder()
                .opportunityType(type)
                .opportunityId(opportunityId)
                .companyId(opportunity.companyId())
                .requirements(opportunity.requirements())
                .directApply(opportunity.requirements().isEmpty())
                .build();
    }

    private List<ApplicationResponse> searchBase(String companyId, String query) {
        Query mongoQuery = new Query();
        mongoQuery.addCriteria(Criteria.where("deleted").is(false));
        if (StringUtils.hasText(companyId)) {
            mongoQuery.addCriteria(Criteria.where("companyId").is(companyId));
        }
        if (StringUtils.hasText(query)) {
            String escaped = java.util.regex.Pattern.quote(query.trim());
            mongoQuery.addCriteria(new Criteria().orOperator(
                    Criteria.where("studentName").regex(escaped, "i"),
                    Criteria.where("studentEmail").regex(escaped, "i"),
                    Criteria.where("opportunityId").regex(escaped, "i")
            ));
        }
        mongoQuery.limit(200);

        return mongoTemplate.find(mongoQuery, Application.class).stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    private Application findVisibleApplicationById(String applicationId) {
        Application application = findApplicationById(applicationId);
        if (application.isDeleted()) {
            throw new ResourceNotFoundException("error.application.not_found", applicationId);
        }
        return application;
    }

    private Application findApplicationById(String applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("error.application.not_found", applicationId));
    }

    private Student findCurrentStudent(String studentId) {
        if (!StringUtils.hasText(studentId)) {
            throw new BadRequestException("error.student.not_found", studentId);
        }

        Student student = findStudentById(studentId);
        if (!SecurityUtils.getCurrentUserId().equals(student.getUserId())) {
            throw new UnauthorizedException("error.auth.forbidden");
        }
        if (student.isDeleted()) {
            throw new ResourceNotFoundException("error.student.not_found", studentId);
        }
        return student;
    }

    private Student findStudentById(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", studentId));
    }

    private OpportunityContext resolveOpportunity(OpportunityType type, String opportunityId) {
        if (type == OpportunityType.JOB) {
            Job job = jobRepository.findById(opportunityId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.job.not_found", opportunityId));
            if (job.isDeleted()) {
                throw new ResourceNotFoundException("error.job.not_found", opportunityId);
            }
            return new OpportunityContext(job.getCompanyId(), job.getJobTitle(), safeList(job.getApplicationRequirements()));
        }

        if (type == OpportunityType.INTERNSHIP) {
            Internship internship = internshipRepository.findById(opportunityId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.internship.not_found", opportunityId));
            if (internship.isDeleted()) {
                throw new ResourceNotFoundException("error.internship.not_found", opportunityId);
            }
            return new OpportunityContext(internship.getCompanyId(), internship.getInternshipTitle(), safeList(internship.getApplicationRequirements()));
        }

        throw new BadRequestException("error.application.opportunity_type.invalid");
    }

    private void validateSubmissionAgainstRequirements(CreateApplicationRequest request, List<String> requirements) {
        if (requirements.isEmpty()) {
            return;
        }

        List<String> missing = new ArrayList<>();
        for (String requirement : requirements) {
            String normalized = normalizeRequirement(requirement);
            switch (normalized) {
                case "RESUME", "CV", "CURRICULUM_VITAE" -> {
                    if (!StringUtils.hasText(request.getResumeFileId())) {
                        missing.add("resumeFileId");
                    }
                }
                case "COVER_LETTER", "COVERLETTER", "LETTER" -> {
                    if (request.getCoverLetter() == null && request.getNote() == null) {
                        missing.add("coverLetter");
                    }
                }
                case "PORTFOLIO", "PORTFOLIO_LINK" -> {
                    if (!StringUtils.hasText(request.getPortfolioLink())) {
                        missing.add("portfolioLink");
                    }
                }
                default -> {
                    // Unknown requirement types are ignored so the backend stays forward-compatible.
                }
            }
        }

        if (!missing.isEmpty()) {
            throw new BadRequestException("error.application.requirements.missing", String.join(",", missing));
        }
    }

    private String normalizeRequirement(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private OpportunityType parseOpportunityType(String opportunityType) {
        try {
            return OpportunityType.valueOf(opportunityType.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("error.application.opportunity_type.invalid");
        }
    }

    private void ensureCanManageApplication(Application application) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }
        if (SecurityUtils.hasRole(Role.STUDENT.name())) {
            Student student = findStudentById(application.getStudentId());
            if (SecurityUtils.getCurrentUserId().equals(student.getUserId())) {
                return;
            }
        }
        if (SecurityUtils.hasRole(Role.COMPANY_ADMIN.name())) {
            Company company = findCompanyById(application.getCompanyId());
            if (SecurityUtils.getCurrentUserId().equals(company.getAdminUserId())) {
                return;
            }
        }
        throw new UnauthorizedException("error.auth.forbidden");
    }

    private void ensureCanViewApplication(Application application) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }
        if (SecurityUtils.hasRole(Role.STUDENT.name())) {
            Student student = findStudentById(application.getStudentId());
            if (SecurityUtils.getCurrentUserId().equals(student.getUserId())) {
                return;
            }
        }
        if (SecurityUtils.hasRole(Role.COMPANY_ADMIN.name())) {
            Company company = findCompanyById(application.getCompanyId());
            if (SecurityUtils.getCurrentUserId().equals(company.getAdminUserId())) {
                return;
            }
        }
        throw new UnauthorizedException("error.auth.forbidden");
    }

    private void ensureCanAccessCompanyScope(String companyId) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }
        if (SecurityUtils.hasRole(Role.COMPANY_ADMIN.name())) {
            Company company = findCompanyById(companyId);
            if (SecurityUtils.getCurrentUserId().equals(company.getAdminUserId())) {
                return;
            }
        }
        throw new UnauthorizedException("error.auth.forbidden");
    }

    private void ensureCanAccessStudentScope(Student student) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }
        if (SecurityUtils.hasRole(Role.STUDENT.name()) && SecurityUtils.getCurrentUserId().equals(student.getUserId())) {
            return;
        }
        throw new UnauthorizedException("error.auth.forbidden");
    }

    private Company findCompanyById(String companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.not_found", companyId));
    }

    private void ensureSuperAdmin() {
        if (!SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new UnauthorizedException("error.auth.forbidden");
        }
    }

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .companyId(application.getCompanyId())
                .opportunityId(application.getOpportunityId())
                .studentId(application.getStudentId())
                .opportunityType(application.getOpportunityType())
                .status(application.getStatus())
                .studentName(application.getStudentName())
                .studentImage(application.getStudentImage())
                .studentEmail(application.getStudentEmail())
                .resumeFileId(application.getResumeFileId())
                .portfolioLink(application.getPortfolioLink())
                .coverLetter(application.getCoverLetter())
                .companyNote(application.getCompanyNote())
                .rejectionReason(application.getRejectionReason())
                .deleted(application.isDeleted())
                .reviewedAt(application.getReviewedAt())
                .decisionAt(application.getDecisionAt())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private record OpportunityContext(String companyId, Object title, List<String> requirements) {
    }
}
