package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateInternshipRequest;
import com.manus.digitalecosystem.dto.request.UpdateInternshipRequest;
import com.manus.digitalecosystem.dto.response.InternshipResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Internship;
import com.manus.digitalecosystem.model.enums.NotificationType;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.InternshipRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.service.InternshipService;
import com.manus.digitalecosystem.service.NotificationService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class InternshipServiceImpl implements InternshipService {

    private final InternshipRepository internshipRepository;
    private final CompanyRepository companyRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public InternshipServiceImpl(
            InternshipRepository internshipRepository,
            CompanyRepository companyRepository,
            StudentRepository studentRepository,
            NotificationService notificationService
    ) {
        this.internshipRepository = internshipRepository;
        this.companyRepository = companyRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    @Override
    public InternshipResponse createInternship(CreateInternshipRequest request) {
        Company myCompany = getMyCompanyOrThrow();
        if (myCompany.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new AccessDeniedException("Forbidden");
        }

        Internship internship = Internship.builder()
                .companyId(myCompany.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .roadmap(request.getRoadmap())
                .duration(request.getDuration())
                .requiredSkills(request.getRequiredSkills())
                .build();

        Internship saved = internshipRepository.save(internship);
        notifyStudents(saved);
        return InternshipResponse.fromInternship(saved);
    }

    @Override
    public InternshipResponse updateInternship(String id, UpdateInternshipRequest request) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.internship.not_found", id));

        if (!SecurityUtils.hasRole("SUPER_ADMIN")) {
            Company myCompany = getMyCompanyOrThrow();
            if (!myCompany.getId().equals(internship.getCompanyId())) {
                throw new AccessDeniedException("Forbidden");
            }
        }

        internship.setTitle(request.getTitle());
        internship.setDescription(request.getDescription());
        internship.setRequirements(request.getRequirements());
        internship.setRoadmap(request.getRoadmap());
        internship.setDuration(request.getDuration());
        internship.setRequiredSkills(request.getRequiredSkills());

        return InternshipResponse.fromInternship(internshipRepository.save(internship));
    }

    @Override
    public void deleteInternship(String id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.internship.not_found", id));

        if (!SecurityUtils.hasRole("SUPER_ADMIN")) {
            Company myCompany = getMyCompanyOrThrow();
            if (!myCompany.getId().equals(internship.getCompanyId())) {
                throw new AccessDeniedException("Forbidden");
            }
        }

        internshipRepository.deleteById(id);
    }

    @Override
    public InternshipResponse getInternshipById(String id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.internship.not_found", id));
        return InternshipResponse.fromInternship(internship);
    }

    @Override
    public PagedResponse<InternshipResponse> searchInternships(String q, String companyId, Pageable pageable) {
        Page<Internship> page;
        if (companyId != null && !companyId.isBlank()) {
            if (q == null || q.isBlank()) {
                page = internshipRepository.findByCompanyId(companyId, pageable);
            } else {
                page = internshipRepository.findByCompanyIdAndTitleContainingIgnoreCase(companyId, q, pageable);
            }
        } else {
            if (q == null || q.isBlank()) {
                page = internshipRepository.findAll(pageable);
            } else {
                page = internshipRepository.findByTitleContainingIgnoreCase(q, pageable);
            }
        }

        return PagedResponse.fromPage(page.map(InternshipResponse::fromInternship));
    }

    private Company getMyCompanyOrThrow() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return companyRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.profile.not_found"));
    }

    private void notifyStudents(Internship internship) {
        String message = "Internship: " + internship.getTitle();
        List<String> studentUserIds = studentRepository.findByVerificationStatus(VerificationStatus.APPROVED).stream()
                .map(Student::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        for (String userId : studentUserIds) {
            if (userId == null || userId.isBlank()) {
                continue;
            }
            notificationService.createNotification(
                    userId,
                    NotificationType.OPPORTUNITY,
                    "notification.opportunity.new.title",
                    "notification.opportunity.new.body",
                    List.of(message)
            );
        }
    }
}
