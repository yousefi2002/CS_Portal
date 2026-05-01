package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateCourseRequest;
import com.manus.digitalecosystem.dto.request.UpdateCourseRequest;
import com.manus.digitalecosystem.dto.response.CourseResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Course;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.CourseRepository;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.CourseService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final UniversityRepository universityRepository;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public CourseServiceImpl(CourseRepository courseRepository,
                             DepartmentRepository departmentRepository,
                             UniversityRepository universityRepository,
                             @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                             @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.universityRepository = universityRepository;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    public CourseResponse createCourse(CreateCourseRequest request, MultipartFile image) {
        validateCourseImage(image);
        Department department = findDepartmentById(request.getDepartmentId());
        ensureCanManageDepartment(department);

        Course course = Course.builder()
                .departmentId(request.getDepartmentId())
                .code(request.getCode())
                .credits(request.getCredits())
                .title(request.getTitle())
                .description(request.getDescription())
                .outcomes(request.getOutcomes())
                .skills(request.getSkills())
                .prerequisites(request.getPrerequisites())
                .build();

        Course savedCourse = courseRepository.save(course);
        try {
            savedCourse.setRoadMapImage(storeCourseImage(savedCourse.getId(), image));
            return toResponse(courseRepository.save(savedCourse));
        } catch (RuntimeException ex) {
            courseRepository.delete(savedCourse);
            deleteCourseImageDirectory(savedCourse.getId());
            throw ex;
        }
    }

    @Override
    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request) {
        Course course = findCourseById(courseId);
        Department department = findDepartmentById(course.getDepartmentId());
        ensureCanManageDepartment(department);

        if (request.getCode() != null) {
            course.setCode(request.getCode());
        }
        if (request.getCredits() != null) {
            course.setCredits(request.getCredits());
        }
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }
        course.setDescription(request.getDescription());
        course.setOutcomes(request.getOutcomes());
        course.setSkills(request.getSkills());
        course.setPrerequisites(request.getPrerequisites());

        return toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse updateCourseImage(String courseId, MultipartFile image) {
        validateCourseImage(image);

        Course course = findCourseById(courseId);
        Department department = findDepartmentById(course.getDepartmentId());
        ensureCanManageDepartment(department);

        deleteCourseImageDirectory(course.getId());
        course.setRoadMapImage(storeCourseImage(course.getId(), image));

        return toResponse(courseRepository.save(course));
    }

    @Override
    public List<CourseResponse> getAllCourses() {
        ensureSuperAdmin();
        return courseRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CourseResponse getCourseById(String courseId) {
        Course course = findCourseById(courseId);
        ensureCanViewCourse(course);
        return toResponse(course);
    }

    @Override
    public List<CourseResponse> getCoursesByDepartment(String departmentId) {
        Department department = findDepartmentById(departmentId);
        ensureCanViewDepartment(department);
        return courseRepository.findByDepartmentId(departmentId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<CourseResponse> getCoursesByUniversity(String universityId) {
        University university = findUniversityById(universityId);
        ensureCanViewUniversity(university);

        List<String> departmentIds = departmentRepository.findByUniversityId(universityId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(department -> !department.isDeleted() || SecurityUtils.hasRole(Role.SUPER_ADMIN.name()))
                .map(Department::getId)
                .toList();

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            Department ownedDepartment = departmentRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                    .orElseThrow(() -> new UnauthorizedException("error.department.forbidden"));
            if (ownedDepartment.isDeleted()) {
                throw new ResourceNotFoundException("error.department.not_found", ownedDepartment.getId());
            }
            if (!universityId.equals(ownedDepartment.getUniversityId())) {
                throw new UnauthorizedException("error.department.forbidden");
            }
            return courseRepository.findByDepartmentId(ownedDepartment.getId()).stream().map(this::toResponse).toList();
        }

        return courseRepository.findByDepartmentIdIn(departmentIds).stream().map(this::toResponse).toList();
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .departmentId(course.getDepartmentId())
                .code(course.getCode())
                .credits(course.getCredits())
                .title(course.getTitle())
                .description(course.getDescription())
                .roadMapImage(course.getRoadMapImage())
                .outcomes(course.getOutcomes())
                .skills(course.getSkills())
                .prerequisites(course.getPrerequisites())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private Course findCourseById(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("error.course.not_found", courseId));
    }

    private Department findDepartmentById(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));

        if (department.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.department.not_found", departmentId);
        }

        return department;
    }

    private University findUniversityById(String universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", universityId));
    }

    private void ensureCanManageDepartment(Department department) {
        if (department.isDeleted()) {
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
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
            return;
        }

        throw new UnauthorizedException("error.department.forbidden");
    }

    private void ensureCanViewCourse(Course course) {
        Department department = findDepartmentById(course.getDepartmentId());
        ensureCanViewDepartment(department);
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

    private void ensureSuperAdmin() {
        if (!SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new UnauthorizedException("error.auth.forbidden");
        }
    }

    private void validateCourseImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("error.course.image.required");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("error.course.image.invalid");
        }
    }

    private String storeCourseImage(String courseId, MultipartFile image) {
        Path directory = resolveCourseImageDirectory(courseId);
        try {
            Files.createDirectories(directory);
            String extension = getSafeExtension(image.getOriginalFilename());
            String filename = generateImageName(extension);
            Path target = directory.resolve(filename);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Path relativePath = uploadBasePath.relativize(target);
            String uploadFolderName = uploadBasePath.getFileName() == null ? "uploads" : uploadBasePath.getFileName().toString();
            return uploadBaseUrl + "/" + uploadFolderName + "/" + relativePath.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new BadRequestException("error.file.upload_failed");
        }
    }

    private void deleteCourseImageDirectory(String courseId) {
        Path directory = resolveCourseImageDirectory(courseId);
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

    private Path resolveCourseImageDirectory(String courseId) {
        return uploadBasePath.resolve("course").resolve(courseId).normalize();
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
}