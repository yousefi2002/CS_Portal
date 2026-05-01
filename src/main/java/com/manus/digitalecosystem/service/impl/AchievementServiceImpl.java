package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateAchievementRequest;
import com.manus.digitalecosystem.dto.request.DeleteAchievementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAchievementRequest;
import com.manus.digitalecosystem.dto.response.AchievementResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Achievement;
import com.manus.digitalecosystem.model.AchievementContributor;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.AchievementRepository;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.AchievementService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class AchievementServiceImpl implements AchievementService {

    private static final int MIN_IMAGES = 1;
    private static final int MAX_IMAGES = 5;

    private final AchievementRepository achievementRepository;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final com.manus.digitalecosystem.repository.StudentRepository studentRepository;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public AchievementServiceImpl(AchievementRepository achievementRepository,
                                  UniversityRepository universityRepository,
                                  DepartmentRepository departmentRepository,
                                  CompanyRepository companyRepository,
                                  com.manus.digitalecosystem.repository.StudentRepository studentRepository,
                                  @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                                  @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.achievementRepository = achievementRepository;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.studentRepository = studentRepository;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    public AchievementResponse createAchievement(CreateAchievementRequest request, List<MultipartFile> images) {
        validateImages(images);
        // If current user is a student, allow creating an achievement for themselves
        AchievementOwnership ownership = null;
        if (SecurityUtils.hasRole(com.manus.digitalecosystem.model.enums.Role.STUDENT.name())) {
            var currentUserId = SecurityUtils.getCurrentUserId();
            var student = studentRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));

            ownership = new AchievementOwnership(student.getUniversityId(), student.getDepartmentId(), null);

            // ensure contributors include the student
            if (request.getContributors() == null || request.getContributors().stream().noneMatch(c -> student.getId().equals(c.getStudentId()))) {
                var contrib = AchievementContributor.builder()
                        .studentId(student.getId())
                        .name(student.getFullName())
                        .imageFileId(student.getImageFileId())
                        .role("student")
                        .isExternal(false)
                        .build();
                var list = request.getContributors() == null ? new ArrayList<AchievementContributor>() : new ArrayList<>(request.getContributors());
                list.add(contrib);
                request.setContributors(list);
            }
        } else {
            ownership = resolveCreateOwnership(request);
        }

        Achievement achievement = Achievement.builder()
                .universityId(ownership.universityId())
                .departmentId(ownership.departmentId())
                .companyId(ownership.companyId())
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .link(request.getLink())
                .contributors(copyContributors(request.getContributors()))
                .build();

        Achievement savedAchievement = achievementRepository.save(achievement);
        try {
            savedAchievement.setImageUrls(storeAchievementImages(savedAchievement.getId(), images));
            return toResponse(achievementRepository.save(savedAchievement));
        } catch (RuntimeException ex) {
            achievementRepository.delete(savedAchievement);
            deleteAchievementImageDirectory(savedAchievement.getId());
            throw ex;
        }
    }

    @Override
    public List<AchievementResponse> getAchievementsByStudent(String studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", studentId));

        // if caller is student, ensure they only request their own
        if (SecurityUtils.hasRole(com.manus.digitalecosystem.model.enums.Role.STUDENT.name())) {
            var currentUserId = SecurityUtils.getCurrentUserId();
            if (!currentUserId.equals(student.getUserId())) {
                throw new UnauthorizedException("error.auth.forbidden");
            }
        }

        return achievementRepository.findByContributorsStudentIdAndDeletedFalse(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AchievementResponse updateAchievementData(String achievementId, UpdateAchievementRequest request) {
        Achievement achievement = findAchievementById(achievementId);
        ensureCanManageAchievement(achievement);

        if (request.getTitle() != null) {
            achievement.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            achievement.setDescription(request.getDescription());
        }
        if (request.getLink() != null) {
            achievement.setLink(request.getLink());
        }
        if (request.getContributors() != null) {
            achievement.setContributors(copyContributors(request.getContributors()));
        }
        if (request.getType() != null) {
            achievement.setType(request.getType());
        }

        return toResponse(achievementRepository.save(achievement));
    }

    @Override
    public AchievementResponse updateAchievementImages(String achievementId, List<MultipartFile> images) {
        validateImages(images);

        Achievement achievement = findAchievementById(achievementId);
        ensureCanManageAchievement(achievement);

        deleteAchievementImageDirectory(achievement.getId());
        achievement.setImageUrls(storeAchievementImages(achievement.getId(), images));

        return toResponse(achievementRepository.save(achievement));
    }

    @Override
    public List<AchievementResponse> getAllAchievements() {
        ensureSuperAdmin();
        return achievementRepository.findAll().stream()
                .sorted(Comparator.comparing(Achievement::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AchievementResponse getAchievementById(String achievementId) {
        return toResponse(findAchievementById(achievementId));
    }

    @Override
    public List<AchievementResponse> getAchievementsByUniversity(String universityId) {
        University university = findUniversityById(universityId);
        ensureCanViewUniversity(university);

        List<String> departmentIds = departmentRepository.findByUniversityId(universityId, Pageable.unpaged()).stream()
                .filter(department -> !department.isDeleted() || SecurityUtils.hasRole(Role.SUPER_ADMIN.name()))
                .map(Department::getId)
                .toList();

        List<Achievement> achievements = new ArrayList<>();
        achievements.addAll(achievementRepository.findByUniversityIdAndDeletedFalse(universityId));
        achievements.addAll(achievementRepository.findByDepartmentIdInAndDeletedFalse(departmentIds));

        return deduplicateAchievements(achievements).stream().map(this::toResponse).toList();
    }

    @Override
    public List<AchievementResponse> getAchievementsByDepartment(String departmentId) {
        Department department = findDepartmentById(departmentId);
        ensureCanViewDepartment(department);

        return achievementRepository.findByDepartmentIdAndDeletedFalse(departmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AchievementResponse> getAchievementsByCompany(String companyId) {
        findCompanyById(companyId);
        return achievementRepository.findByCompanyIdAndDeletedFalse(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteAchievement(String achievementId) {
        ensureSuperAdmin();
        Achievement achievement = findAchievementById(achievementId);
        achievementRepository.delete(achievement);
        deleteAchievementImageDirectory(achievement.getId());
    }

    @Override
    public AchievementResponse softDeleteAchievement(String achievementId, DeleteAchievementRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getDeleted())) {
            throw new BadRequestException("error.achievement.deleted.required");
        }

        Achievement achievement = findAchievementById(achievementId);
        ensureCanManageAchievement(achievement);

        achievement.setDeleted(true);
        return toResponse(achievementRepository.save(achievement));
    }

    @Override
    public AchievementResponse assignStudentToAchievement(String achievementId, String studentId) {
        Achievement achievement = findAchievementById(achievementId);

        // student must exist
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", studentId));

        // allow if super admin or admin who can manage achievement, or if the current user is the student
        boolean isStudentCaller = student.getUserId() != null && SecurityUtils.getCurrentUserId().equals(student.getUserId());
        if (!isStudentCaller) {
            ensureCanManageAchievement(achievement);
        }

        if (achievement.getContributors() == null) {
            achievement.setContributors(new ArrayList<>());
        }

        boolean already = achievement.getContributors().stream()
                .anyMatch(c -> c.getStudentId() != null && c.getStudentId().equals(studentId));
        if (!already) {
            AchievementContributor contributor = AchievementContributor.builder()
                    .studentId(student.getId())
                    .name(student.getFullName())
                    .imageFileId(student.getImageFileId())
                    .role("student")
                    .isExternal(false)
                    .build();
            achievement.getContributors().add(contributor);
            achievement = achievementRepository.save(achievement);
        }

        return toResponse(achievement);
    }

    @Override
    public AchievementResponse removeStudentFromAchievement(String achievementId, String studentId) {
        Achievement achievement = findAchievementById(achievementId);

        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", studentId));

        boolean isStudentCaller = student.getUserId() != null && SecurityUtils.getCurrentUserId().equals(student.getUserId());
        if (!isStudentCaller) {
            ensureCanManageAchievement(achievement);
        }

        if (achievement.getContributors() != null) {
            boolean removed = achievement.getContributors().removeIf(c -> c.getStudentId() != null && c.getStudentId().equals(studentId));
            if (removed) {
                achievement = achievementRepository.save(achievement);
            }
        }

        return toResponse(achievement);
    }

    private AchievementResponse toResponse(Achievement achievement) {
        return AchievementResponse.builder()
                .id(achievement.getId())
                .universityId(achievement.getUniversityId())
                .departmentId(achievement.getDepartmentId())
                .companyId(achievement.getCompanyId())
                .type(achievement.getType())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .link(achievement.getLink())
                .imageUrls(achievement.getImageUrls())
                .contributors(achievement.getContributors())
                .deleted(achievement.isDeleted())
                .createdAt(achievement.getCreatedAt())
                .updatedAt(achievement.getUpdatedAt())
                .build();
    }

    private Achievement findAchievementById(String achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("error.achievement.not_found", achievementId));

        if (achievement.isDeleted() && (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) || SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name()))
                && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.achievement.not_found", achievementId);
        }

        return achievement;
    }

    private University findUniversityById(String universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", universityId));
    }

    private Department findDepartmentById(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));

        if (department.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.department.not_found", departmentId);
        }

        return department;
    }

    private Company findCompanyById(String companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.not_found", companyId));
    }

    private void ensureCanManageAchievement(Achievement achievement) {
        // allow contributor student to manage their own achievement
        if (achievement.getContributors() != null && !achievement.getContributors().isEmpty()) {
            for (AchievementContributor c : achievement.getContributors()) {
                if (c.getStudentId() == null) continue;
                var studentOpt = studentRepository.findById(c.getStudentId());
                if (studentOpt.isPresent()) {
                    var student = studentOpt.get();
                    if (SecurityUtils.hasRole(com.manus.digitalecosystem.model.enums.Role.STUDENT.name())
                            && SecurityUtils.getCurrentUserId().equals(student.getUserId())) {
                        return;
                    }
                }
            }
        }
        if (achievement.getCompanyId() != null) {
            Company company = findCompanyById(achievement.getCompanyId());
            if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name()) ||
                    (SecurityUtils.hasRole(Role.COMPANY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(company.getAdminUserId()))) {
                return;
            }
            throw new UnauthorizedException("error.achievement.forbidden");
        }

        if (achievement.getDepartmentId() != null) {
            Department department = findDepartmentById(achievement.getDepartmentId());
            if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
                return;
            }
            if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
                return;
            }
            if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
                University university = findUniversityById(department.getUniversityId());
                if (SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
                    return;
                }
            }
            throw new UnauthorizedException("error.achievement.forbidden");
        }

        if (achievement.getUniversityId() != null) {
            University university = findUniversityById(achievement.getUniversityId());
            if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name()) ||
                    (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(university.getAdminUserId()))) {
                return;
            }
            throw new UnauthorizedException("error.achievement.forbidden");
        }

        throw new BadRequestException("error.achievement.ownership.invalid");
    }

    private AchievementOwnership resolveCreateOwnership(CreateAchievementRequest request) {
        boolean hasUniversityOwnership = StringUtils.hasText(request.getUniversityId());
        boolean hasDepartmentOwnership = StringUtils.hasText(request.getDepartmentId());
        boolean hasCompanyOwnership = StringUtils.hasText(request.getCompanyId());

        int ownershipCount = (hasUniversityOwnership ? 1 : 0) + (hasDepartmentOwnership && !hasCompanyOwnership ? 1 : 0) + (hasCompanyOwnership ? 1 : 0);
        if (ownershipCount != 1) {
            throw new BadRequestException("error.achievement.ownership.invalid");
        }

        if (hasUniversityOwnership) {
            University university = findUniversityById(request.getUniversityId());
            if (!(SecurityUtils.hasRole(Role.SUPER_ADMIN.name()) ||
                    (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())))) {
                throw new UnauthorizedException("error.achievement.forbidden");
            }
            return new AchievementOwnership(request.getUniversityId(), null, null);
        }

        if (hasCompanyOwnership) {
            if (!hasDepartmentOwnership) {
                throw new BadRequestException("error.achievement.ownership.invalid");
            }

            Company company = findCompanyById(request.getCompanyId());
            findDepartmentById(request.getDepartmentId());
            if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name()) ||
                    (SecurityUtils.hasRole(Role.COMPANY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(company.getAdminUserId()))) {
                return new AchievementOwnership(null, request.getDepartmentId(), request.getCompanyId());
            }
            throw new UnauthorizedException("error.achievement.forbidden");
        }

        Department department = findDepartmentById(request.getDepartmentId());
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return new AchievementOwnership(null, request.getDepartmentId(), null);
        }
        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
            return new AchievementOwnership(null, request.getDepartmentId(), null);
        }
        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityById(department.getUniversityId());
            if (SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
                return new AchievementOwnership(null, request.getDepartmentId(), null);
            }
        }
        throw new UnauthorizedException("error.achievement.forbidden");
    }

    private void ensureCanViewUniversity(University university) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            throw new ResourceNotFoundException("error.university.not_found", university.getId());
        }
    }

    private void ensureCanViewDepartment(Department department) {
        if (department.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.department.not_found", department.getId());
        }

        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityById(department.getUniversityId());
            if (SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
                return;
            }
            throw new ResourceNotFoundException("error.department.not_found", department.getId());
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            if (SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
                return;
            }
            throw new ResourceNotFoundException("error.department.not_found", department.getId());
        }
    }

    private void ensureSuperAdmin() {
        if (!SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new UnauthorizedException("error.auth.forbidden");
        }
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new BadRequestException("error.achievement.images.required");
        }

        long validCount = images.stream().filter(image -> image != null && !image.isEmpty()).count();
        if (validCount < MIN_IMAGES) {
            throw new BadRequestException("error.achievement.images.min", MIN_IMAGES);
        }
        if (validCount > MAX_IMAGES) {
            throw new BadRequestException("error.achievement.images.max", MAX_IMAGES);
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                throw new BadRequestException("error.achievement.images.required");
            }
            String contentType = image.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new BadRequestException("error.achievement.images.invalid");
            }
        }
    }

    private List<String> storeAchievementImages(String achievementId, List<MultipartFile> images) {
        Path directory = resolveAchievementImageDirectory(achievementId);
        List<String> storedImages = new ArrayList<>();

        try {
            Files.createDirectories(directory);
            for (MultipartFile image : images) {
                String extension = getSafeExtension(image.getOriginalFilename());
                String filename = generateImageName(extension);
                Path target = directory.resolve(filename);
                Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                Path relativePath = uploadBasePath.relativize(target);
                String uploadFolderName = uploadBasePath.getFileName() == null ? "uploads" : uploadBasePath.getFileName().toString();
                storedImages.add(uploadBaseUrl + "/" + uploadFolderName + "/" + relativePath.toString().replace('\\', '/'));
            }
            return storedImages;
        } catch (IOException ex) {
            throw new BadRequestException("error.file.upload_failed");
        }
    }

    private void deleteAchievementImageDirectory(String achievementId) {
        Path directory = resolveAchievementImageDirectory(achievementId);
        if (!Files.exists(directory)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private Path resolveAchievementImageDirectory(String achievementId) {
        return uploadBasePath.resolve("achievement").resolve(achievementId).normalize();
    }

    private String getSafeExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return ".jpg";
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        if (extension.length() > 8) {
            return ".jpg";
        }

        return extension;
    }

    private String generateImageName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private List<Achievement> deduplicateAchievements(List<Achievement> achievements) {
        Set<String> seenIds = new HashSet<>();
        return achievements.stream()
                .filter(achievement -> achievement.getId() != null && seenIds.add(achievement.getId()))
                .toList();
    }

    private List<AchievementContributor> copyContributors(List<AchievementContributor> contributors) {
        if (contributors == null) {
            return null;
        }

        return contributors.stream()
                .filter(Objects::nonNull)
                .map(contributor -> AchievementContributor.builder()
                        .studentId(contributor.getStudentId())
                        .name(contributor.getName())
                        .imageFileId(contributor.getImageFileId())
                        .role(contributor.getRole())
                        .isExternal(contributor.isExternal())
                        .build())
                .toList();
    }

    private record AchievementOwnership(String universityId, String departmentId, String companyId) {
    }
}