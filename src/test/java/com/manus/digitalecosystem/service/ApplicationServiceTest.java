package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateApplicationRequest;
import com.manus.digitalecosystem.dto.request.DeleteApplicationRequest;
import com.manus.digitalecosystem.dto.request.UpdateApplicationRequest;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Application;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Job;
import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.enums.ApplicationStatus;
import com.manus.digitalecosystem.model.enums.OpportunityType;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.ApplicationRepository;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.InternshipRepository;
import com.manus.digitalecosystem.repository.JobRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.security.UserDetailsImpl;
import com.manus.digitalecosystem.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private InternshipRepository internshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        applicationService = new ApplicationServiceImpl(
                applicationRepository,
                studentRepository,
                companyRepository,
                jobRepository,
                internshipRepository,
                userRepository,
                mongoTemplate
        );
    }

    @Test
    void createApplicationRejectsDuplicateSubmission() {
        authenticate(Role.STUDENT, "user-1");

        Student student = Student.builder().id("student-1").userId("user-1").deleted(false).build();
        when(studentRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(Job.builder().id("job-1").companyId("company-1").deleted(false).build()));
        when(applicationRepository.existsByStudentIdAndOpportunityTypeAndOpportunityId("student-1", OpportunityType.JOB, "job-1")).thenReturn(true);

        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setStudentId("student-1");
        request.setOpportunityType(OpportunityType.JOB);
        request.setOpportunityId("job-1");

        assertThrows(DuplicateResourceException.class, () -> applicationService.createApplication(request));
    }

    @Test
    void getDeletedApplicationIsHidden() {
        authenticate(Role.STUDENT, "user-1");

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(Application.builder().id("app-1").deleted(true).studentId("student-1").companyId("company-1").build()));

        assertThrows(ResourceNotFoundException.class, () -> applicationService.getApplicationById("app-1"));
    }

    @Test
    void softDeleteByOwnerStudentWorks() {
        authenticate(Role.STUDENT, "user-1");

        Student student = Student.builder().id("student-1").userId("user-1").deleted(false).build();
        Application application = Application.builder().id("app-1").studentId("student-1").companyId("company-1").deleted(false).status(ApplicationStatus.SUBMITTED).build();

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(application));
        when(studentRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeleteApplicationRequest request = new DeleteApplicationRequest();
        request.setDeleted(true);

        assertEquals(true, applicationService.softDeleteApplication("app-1", request).isDeleted());
    }

    private void authenticate(Role role, String userId) {
        UserDetailsImpl principal = new UserDetailsImpl(
                userId,
                userId + "@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())),
                true
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
