package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.AssignCoursesToSemesterRequest;
import com.manus.digitalecosystem.dto.request.CreateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.CreateSemesterRequest;
import com.manus.digitalecosystem.dto.request.DeleteDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateSemesterRequest;
import com.manus.digitalecosystem.dto.response.DepartmentResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Course;
import com.manus.digitalecosystem.model.CurriculumSemester;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.CourseRepository;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.DepartmentService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final int MIN_DEPARTMENT_IMAGES = 1;
    private static final int MAX_DEPARTMENT_IMAGES = 5;

    private final DepartmentRepository departmentRepository;
    private final UniversityRepository universityRepository;
    private final CourseRepository courseRepository;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 UniversityRepository universityRepository,
                                 CourseRepository courseRepository,
                                 @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                                 @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.departmentRepository = departmentRepository;
        this.universityRepository = universityRepository;
        this.courseRepository = courseRepository;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    public DepartmentResponse createDepartment(CreateDepartmentRequest request, List<MultipartFile> images) {
        if (request == null) {
            throw new BadRequestException("error.department.data.required");
        }

        validateDepartmentImages(images);
        University university = findUniversityById(request.getUniversityId());
        ensureCanManageUniversity(university);

        Department department = Department.builder()
                .universityId(request.getUniversityId())
                .name(request.getName())
                .description(request.getDescription())
                .goals(request.getGoals())
                .outcomes(request.getOutcomes())
                .adminUserId(request.getAdminUserId())
                .build();

        Department savedDepartment = departmentRepository.save(department);
        try {
            savedDepartment.setImageFileIds(storeDepartmentImages(savedDepartment.getId(), images));
            return toResponse(departmentRepository.save(savedDepartment));
        } catch (RuntimeException ex) {
            departmentRepository.delete(savedDepartment);
            deleteDepartmentImageDirectory(savedDepartment.getId());
            throw ex;
        }
    }

    @Override
    public DepartmentResponse updateDepartment(String departmentId, UpdateDepartmentRequest request) {
        Department department = findDepartmentById(departmentId, false);
        ensureCanManageDepartment(department);

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setGoals(request.getGoals());
        department.setOutcomes(request.getOutcomes());
        department.setAdminUserId(request.getAdminUserId());

        return toResponse(departmentRepository.save(department));
    }

    @Override
    public DepartmentResponse updateDepartmentImages(String departmentId, List<MultipartFile> images) {
        validateDepartmentImages(images);

        Department department = findDepartmentById(departmentId, false);
        ensureCanManageDepartment(department);

        deleteDepartmentImageDirectory(department.getId());
        department.setImageFileIds(storeDepartmentImages(department.getId(), images));

        return toResponse(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        ensureSuperAdmin();
        return departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<DepartmentResponse> getDepartmentsByUniversity(String universityId) {
        University university = findUniversityById(universityId);

        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return departmentRepository.findByUniversityId(universityId, Pageable.unpaged()).stream()
                    .sorted(Comparator.comparing(Department::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(this::toResponse)
                    .toList();
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            if (!SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
                throw new ResourceNotFoundException("error.university.not_found", universityId);
            }

            return departmentRepository.findByUniversityIdAndDeletedFalse(universityId, Pageable.unpaged()).stream()
                    .sorted(Comparator.comparing(Department::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(this::toResponse)
                    .toList();
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            return departmentRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                    .filter(department -> universityId.equals(department.getUniversityId()))
                    .map(List::of)
                    .orElseGet(List::of)
                    .stream()
                    .filter(department -> !department.isDeleted())
                    .map(this::toResponse)
                    .toList();
        }

        throw new UnauthorizedException("error.department.forbidden");
    }

    @Override
    public DepartmentResponse getDepartmentById(String departmentId) {
        Department department = findDepartmentById(departmentId, true);
        ensureCanViewDepartment(department);
        return toResponse(department);
    }

    @Override
    public void deleteDepartment(String departmentId) {
        ensureSuperAdmin();
        Department department = findDepartmentById(departmentId, true);
        courseRepository.deleteAll(courseRepository.findByDepartmentId(departmentId));
        departmentRepository.delete(department);
        deleteDepartmentImageDirectory(departmentId);
    }

    @Override
    public DepartmentResponse softDeleteDepartment(String departmentId, DeleteDepartmentRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getDeleted())) {
            throw new BadRequestException("error.department.deleted.required");
        }

        Department department = findDepartmentById(departmentId, false);
        ensureUniversityAdminForDepartment(department);

        department.setDeleted(true);
        return toResponse(departmentRepository.save(department));
    }

    @Override
    public CurriculumSemester createSemester(String departmentId, CreateSemesterRequest request) {
        Department department = findDepartmentById(departmentId, false);
        ensureCanManageDepartment(department);

        if (request == null || request.getNumber() == null) {
            throw new BadRequestException("error.semester.data.required");
        }

        List<CurriculumSemester> semesters = getOrCreateSemesters(department);
        if (semesters.stream().anyMatch(semester -> semester.getNumber() == request.getNumber())) {
            throw new DuplicateResourceException("error.semester.exists", request.getNumber());
        }

        CurriculumSemester semester = CurriculumSemester.builder()
                .number(request.getNumber())
                .goals(request.getGoals())
                .outcomes(request.getOutcomes())
                .courseIds(request.getCourseIds())
                .build();
        semesters.add(semester);
        department.setSemesters(sortSemesters(semesters));
        departmentRepository.save(department);
        return semester;
    }

    @Override
    public CurriculumSemester updateSemester(String departmentId, int number, UpdateSemesterRequest request) {
        Department department = findDepartmentById(departmentId, false);
        ensureCanManageDepartment(department);

        CurriculumSemester semester = findSemester(department, number);
        semester.setGoals(request.getGoals());
        semester.setOutcomes(request.getOutcomes());
        semester.setCourseIds(request.getCourseIds());
        department.setSemesters(sortSemesters(getOrCreateSemesters(department)));
        departmentRepository.save(department);
        return semester;
    }

    @Override
    public void deleteSemester(String departmentId, int number) {
        Department department = findDepartmentById(departmentId, false);
        ensureCanManageDepartment(department);

        List<CurriculumSemester> semesters = getOrCreateSemesters(department);
        boolean removed = semesters.removeIf(semester -> semester.getNumber() == number);
        if (!removed) {
            throw new ResourceNotFoundException("error.semester.not_found", number);
        }

        department.setSemesters(sortSemesters(semesters));
        departmentRepository.save(department);
    }

    @Override
    public List<CurriculumSemester> getSemesters(String departmentId) {
        Department department = findDepartmentById(departmentId, true);
        ensureCanViewDepartment(department);
        return sortSemesters(getOrCreateSemesters(department));
    }

    @Override
    public CurriculumSemester assignCoursesToSemester(String departmentId, int number, AssignCoursesToSemesterRequest request) {
        Department department = findDepartmentById(departmentId, false);
        ensureCanManageDepartment(department);

        if (request == null || request.getCourseIds() == null || request.getCourseIds().isEmpty()) {
            throw new BadRequestException("error.course.ids.required");
        }

        List<Course> courses = courseRepository.findAllById(request.getCourseIds()).stream().toList();
        if (courses.size() != request.getCourseIds().size()) {
            throw new ResourceNotFoundException("error.course.not_found", departmentId);
        }

        for (Course course : courses) {
            if (!departmentId.equals(course.getDepartmentId())) {
                throw new BadRequestException("error.course.department.mismatch");
            }
        }

        CurriculumSemester semester = findSemester(department, number);
        semester.setCourseIds(new ArrayList<>(request.getCourseIds()));
        department.setSemesters(sortSemesters(getOrCreateSemesters(department)));
        departmentRepository.save(department);
        return semester;
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .universityId(department.getUniversityId())
                .name(department.getName())
                .description(department.getDescription())
                .imageFileIds(department.getImageFileIds())
                .adminUserId(department.getAdminUserId())
                .goals(department.getGoals())
                .outcomes(department.getOutcomes())
                .semesters(sortSemesters(getOrCreateSemesters(department)))
                .deleted(department.isDeleted())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    private Department findDepartmentById(String departmentId, boolean includeDeleted) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));

        if (!includeDeleted && department.isDeleted()) {
            throw new ResourceNotFoundException("error.department.not_found", departmentId);
        }

        return department;
    }

    private University findUniversityById(String universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", universityId));
    }

    private void ensureCanManageUniversity(University university) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
            return;
        }

        throw new UnauthorizedException("error.university.forbidden");
    }

    private void ensureCanManageDepartment(Department department) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityById(department.getUniversityId());
            if (SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
                return;
            }
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
            return;
        }

        throw new UnauthorizedException("error.department.forbidden");
    }

    private void ensureUniversityAdminForDepartment(Department department) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityById(department.getUniversityId());
            if (SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
                return;
            }
        }

        throw new UnauthorizedException("error.department.forbidden");
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

    private void validateDepartmentImages(List<MultipartFile> images) {
        if (images == null || images.size() < MIN_DEPARTMENT_IMAGES) {
            throw new BadRequestException("error.department.images.min", MIN_DEPARTMENT_IMAGES);
        }

        if (images.size() > MAX_DEPARTMENT_IMAGES) {
            throw new BadRequestException("error.department.images.max", MAX_DEPARTMENT_IMAGES);
        }

        for (MultipartFile image : images) {
            validateImage(image);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("error.department.images.invalid");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("error.department.images.invalid");
        }
    }

    private List<String> storeDepartmentImages(String departmentId, List<MultipartFile> images) {
        Path directory = resolveDepartmentImageDirectory(departmentId);
        try {
            Files.createDirectories(directory);
            List<String> storedPaths = new ArrayList<>();

            for (MultipartFile image : images) {
                String extension = getSafeExtension(image.getOriginalFilename());
                String filename = generateImageName(extension);
                Path target = directory.resolve(filename);
                Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                storedPaths.add(toPublicImagePath(target));
            }

            return storedPaths;
        } catch (IOException ex) {
            throw new BadRequestException("error.file.upload_failed");
        }
    }

    private String toPublicImagePath(Path target) {
        Path relativePath = uploadBasePath.relativize(target);
        String uploadFolderName = uploadBasePath.getFileName() == null ? "uploads" : uploadBasePath.getFileName().toString();
        return uploadBaseUrl + "/" + uploadFolderName + "/" + relativePath.toString().replace('\\', '/');
    }

    private void deleteDepartmentImageDirectory(String departmentId) {
        Path directory = resolveDepartmentImageDirectory(departmentId);
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

    private Path resolveDepartmentImageDirectory(String departmentId) {
        return uploadBasePath.resolve("department").resolve(departmentId).normalize();
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

    private List<CurriculumSemester> getOrCreateSemesters(Department department) {
        if (department.getSemesters() == null) {
            department.setSemesters(new ArrayList<>());
        }
        return department.getSemesters();
    }

    private CurriculumSemester findSemester(Department department, int number) {
        return getOrCreateSemesters(department).stream()
                .filter(semester -> semester.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("error.semester.not_found", number));
    }

    private List<CurriculumSemester> sortSemesters(List<CurriculumSemester> semesters) {
        if (semesters == null) {
            return new ArrayList<>();
        }

        semesters.sort(Comparator.comparingInt(CurriculumSemester::getNumber));
        return semesters;
    }
}