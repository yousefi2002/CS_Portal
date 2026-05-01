package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateAchievementRequest;
import com.manus.digitalecosystem.dto.request.DeleteAchievementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAchievementRequest;
import com.manus.digitalecosystem.dto.response.AchievementResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.model.Achievement;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.AchievementRepository;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.security.UserDetailsImpl;
import com.manus.digitalecosystem.service.impl.AchievementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAchievementRejectsTooManyImages() {
        authenticate(Role.COMPANY_ADMIN, "company-admin-1");

        CreateAchievementRequest request = new CreateAchievementRequest();
        request.setCompanyId("company-1");
        request.setDepartmentId("department-1");
        request.setType("award");
        request.setTitle(new LocalizedText("Title FA", "Title EN", "Title PS"));

        when(companyRepository.findById("company-1")).thenReturn(Optional.of(Company.builder().id("company-1").adminUserId("company-admin-1").build()));
        when(departmentRepository.findById("department-1")).thenReturn(Optional.of(Department.builder().id("department-1").universityId("university-1").deleted(false).build()));

        List<MultipartFile> images = new ArrayList<>();
        images.add(image("one.png"));
        images.add(image("two.png"));
        images.add(image("three.png"));
        images.add(image("four.png"));
        images.add(image("five.png"));
        images.add(image("six.png"));

        assertThrows(BadRequestException.class, () -> achievementService.createAchievement(request, images));
        verify(achievementRepository, never()).save(any(Achievement.class));
    }

    @Test
    void getAchievementsByUniversityExcludesDeletedAchievements() {
        authenticate(Role.SUPER_ADMIN, "super-admin");

        University university = University.builder().id("university-1").adminUserId("university-admin").build();
        Department activeDepartment = Department.builder().id("department-1").universityId("university-1").deleted(false).build();
        Achievement universityAchievement = Achievement.builder().id("achievement-1").universityId("university-1").deleted(false).build();
        Achievement departmentAchievement = Achievement.builder().id("achievement-2").departmentId("department-1").deleted(false).build();

        when(universityRepository.findById("university-1")).thenReturn(Optional.of(university));
        when(departmentRepository.findByUniversityId("university-1", org.springframework.data.domain.Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(activeDepartment)));
        when(achievementRepository.findByUniversityIdAndDeletedFalse("university-1")).thenReturn(List.of(universityAchievement));
        when(achievementRepository.findByDepartmentIdInAndDeletedFalse(List.of("department-1"))).thenReturn(List.of(departmentAchievement));

        List<AchievementResponse> responses = achievementService.getAchievementsByUniversity("university-1");

        assertEquals(2, responses.size());
        verify(achievementRepository, times(1)).findByUniversityIdAndDeletedFalse("university-1");
    }

    @Test
    void softDeleteAchievementRequiresTrueFlag() {
        authenticate(Role.SUPER_ADMIN, "super-admin");

        DeleteAchievementRequest request = new DeleteAchievementRequest();
        request.setDeleted(false);

        assertThrows(BadRequestException.class, () -> achievementService.softDeleteAchievement("achievement-1", request));
    }

    @Test
    void updateAchievementDataAllowsPartialUpdate() {
        authenticate(Role.SUPER_ADMIN, "super-admin");

        Achievement achievement = Achievement.builder().id("achievement-1").universityId("university-1").deleted(false).build();
        UpdateAchievementRequest request = new UpdateAchievementRequest();
        request.setType("new-type");

        when(achievementRepository.findById("achievement-1")).thenReturn(Optional.of(achievement));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AchievementResponse response = achievementService.updateAchievementData("achievement-1", request);

        assertEquals("new-type", response.getType());
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

    private MockMultipartFile image(String filename) {
        return new MockMultipartFile("images", filename, "image/png", new byte[] {1, 2, 3});
    }
}