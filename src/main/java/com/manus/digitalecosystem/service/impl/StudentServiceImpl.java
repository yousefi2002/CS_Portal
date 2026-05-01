package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateStudentAccountRequest;
import com.manus.digitalecosystem.dto.request.DeleteStudentRequest;
import com.manus.digitalecosystem.dto.request.UpdateStudentRequest;
import com.manus.digitalecosystem.dto.response.StudentResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.service.StudentService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public StudentServiceImpl(StudentRepository studentRepository,
                              UserRepository userRepository,
                              UniversityRepository universityRepository,
                              DepartmentRepository departmentRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                              @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    @Transactional
    public StudentResponse createStudent(CreateStudentAccountRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("error.user.email.exists", request.getEmail());
        }

        Department department = findDepartmentById(request.getDepartmentId());
        if (department.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.department.not_found", request.getDepartmentId());
        }

        if (!department.getUniversityId().equals(request.getUniversityId())) {
            throw new BadRequestException("error.department.university_mismatch");
        }

        University university = findUniversityById(request.getUniversityId());
        ensureCanManageWithinScope(university, department);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .status(Status.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        try {
            Student student = Student.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .universityId(request.getUniversityId())
                    .departmentId(request.getDepartmentId())
                    .verificationStatus(VerificationStatus.APPROVED)
                    .skills(new ArrayList<>())
                    .build();
            return toResponse(studentRepository.save(student));
        } catch (RuntimeException ex) {
            userRepository.delete(savedUser);
            throw ex;
        }
    }

    @Override
    public StudentResponse updateStudent(String studentId, UpdateStudentRequest request) {
        Student student = findVisibleStudentById(studentId);
        ensureCanUpdateStudent(student);

        if (StringUtils.hasText(request.getFullName())) {
            student.setFullName(request.getFullName());
            User user = findUserById(student.getUserId());
            user.setFullName(request.getFullName());
            if (StringUtils.hasText(request.getPassword())) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            userRepository.save(user);
        } else if (StringUtils.hasText(request.getPassword())) {
            User user = findUserById(student.getUserId());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }

        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            student.setBio(request.getBio());
        }
        if (request.getSkills() != null) {
            student.setSkills(request.getSkills());
        }

        return toResponse(studentRepository.save(student));
    }

    @Override
    public StudentResponse updateStudentImage(String studentId, MultipartFile image) {
        Student student = findVisibleStudentById(studentId);
        ensureCanUpdateStudent(student);

        validateImage(image);
        String oldImage = student.getImageFileId();
        String newImage = storeStudentImage(studentId, image);
        student.setImageFileId(newImage);
        Student saved = studentRepository.save(student);
        if (oldImage != null && !oldImage.isBlank()) {
            deleteByUrl(oldImage);
        }
        return toResponse(saved);
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        ensureSuperAdmin();
        return studentRepository.findAll().stream()
                .sorted(Comparator.comparing(Student::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StudentResponse getStudentById(String studentId) {
        Student student = findStudentById(studentId);
        if (student.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.student.not_found", studentId);
        }
        return toResponse(student);
    }

    @Override
    public List<StudentResponse> getStudentsByUniversity(String universityId) {
        University university = findUniversityById(universityId);
        ensureCanViewUniversity(university);

        List<Student> students = SecurityUtils.hasRole(Role.SUPER_ADMIN.name())
                ? studentRepository.findByUniversityId(universityId)
                : studentRepository.findByUniversityIdAndDeletedFalse(universityId);

        return students.stream().map(this::toResponse).toList();
    }

    @Override
    public List<StudentResponse> getStudentsByDepartment(String departmentId) {
        Department department = findDepartmentById(departmentId);
        ensureCanViewDepartment(department);

        List<Student> students = SecurityUtils.hasRole(Role.SUPER_ADMIN.name())
                ? studentRepository.findByDepartmentId(departmentId)
                : studentRepository.findByDepartmentIdAndDeletedFalse(departmentId);

        return students.stream().map(this::toResponse).toList();
    }

    @Override
    public StudentResponse softDeleteStudent(String studentId, DeleteStudentRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getDeleted())) {
            throw new BadRequestException("error.student.deleted.required");
        }

        Student student = findVisibleStudentById(studentId);
        ensureCanSoftDeleteStudent(student);

        student.setDeleted(true);
        User user = findUserById(student.getUserId());
        user.setStatus(Status.SUSPENDED);
        userRepository.save(user);

        return toResponse(studentRepository.save(student));
    }

    @Override
    public void hardDeleteStudent(String studentId) {
        ensureSuperAdmin();
        Student student = findStudentById(studentId);

        User user = findUserById(student.getUserId());
        user.setStatus(Status.SUSPENDED);
        userRepository.save(user);

        studentRepository.delete(student);
        if (student.getImageFileId() != null && !student.getImageFileId().isBlank()) {
            deleteByUrl(student.getImageFileId());
        }
    }

    @Override
    public StudentResponse markAsGraduated(String studentId) {
        Student student = findVisibleStudentById(studentId);
        Department department = findDepartmentById(student.getDepartmentId());
        University university = findUniversityById(student.getUniversityId());
        ensureCanManageWithinScope(university, department);

        student.setGraduated(true);
        student.setGraduatedAt(Instant.now());
        return toResponse(studentRepository.save(student));
    }

    @Override
    public List<StudentResponse> searchScoped(String query, String universityId, String departmentId) {
        List<Student> base;
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            if (StringUtils.hasText(departmentId)) {
                base = studentRepository.findByDepartmentId(departmentId);
            } else if (StringUtils.hasText(universityId)) {
                base = studentRepository.findByUniversityId(universityId);
            } else {
                base = studentRepository.findAll();
            }
        } else if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityByAdminUserId(SecurityUtils.getCurrentUserId());
            base = studentRepository.findByUniversityIdAndDeletedFalse(university.getId());
        } else if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            Department department = findDepartmentByAdminUserId(SecurityUtils.getCurrentUserId());
            base = studentRepository.findByDepartmentIdAndDeletedFalse(department.getId());
        } else {
            throw new UnauthorizedException("error.auth.forbidden");
        }

        return filterByQuery(base, query, !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> searchGlobal(String query) {
        ensureSuperAdmin();
        return filterByQuery(studentRepository.findAll(), query, false).stream()
                .map(this::toResponse)
                .toList();
    }

    private List<Student> filterByQuery(List<Student> students, String query, boolean excludeDeleted) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return students.stream()
                .filter(student -> !excludeDeleted || !student.isDeleted())
                .filter(student -> {
                    if (q.isBlank()) {
                        return true;
                    }
                    String name = student.getFullName() == null ? "" : student.getFullName().toLowerCase(Locale.ROOT);
                    String email = student.getEmail() == null ? "" : student.getEmail().toLowerCase(Locale.ROOT);
                    return name.contains(q) || email.contains(q);
                })
                .toList();
    }

    private void ensureCanUpdateStudent(Student student) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.STUDENT.name()) && SecurityUtils.getCurrentUserId().equals(student.getUserId())) {
            return;
        }

        throw new UnauthorizedException("error.auth.forbidden");
    }

    private void ensureCanSoftDeleteStudent(Student student) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.STUDENT.name()) && SecurityUtils.getCurrentUserId().equals(student.getUserId())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityById(student.getUniversityId());
            if (SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
                return;
            }
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            Department department = findDepartmentById(student.getDepartmentId());
            if (SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
                return;
            }
        }

        throw new UnauthorizedException("error.auth.forbidden");
    }

    private void ensureCanManageWithinScope(University university, Department department) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
            return;
        }

        throw new UnauthorizedException("error.auth.forbidden");
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

    private Student findVisibleStudentById(String studentId) {
        Student student = findStudentById(studentId);
        if (student.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.student.not_found", studentId);
        }
        return student;
    }

    private Student findStudentById(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", studentId));
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found", userId));
    }

    private Department findDepartmentById(String departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));
    }

    private Department findDepartmentByAdminUserId(String adminUserId) {
        return departmentRepository.findByAdminUserId(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
    }

    private University findUniversityById(String universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", universityId));
    }

    private University findUniversityByAdminUserId(String adminUserId) {
        return universityRepository.findByAdminUserId(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("error.file.empty");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("error.file.upload_failed");
        }
    }

    private String storeStudentImage(String studentId, MultipartFile image) {
        Path directory = uploadBasePath.resolve("student").resolve(studentId).normalize();
        try {
            Files.createDirectories(directory);
            String extension = getSafeExtension(image.getOriginalFilename());
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path target = directory.resolve(filename);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Path relativePath = uploadBasePath.relativize(target);
            String uploadFolderName = uploadBasePath.getFileName() == null ? "uploads" : uploadBasePath.getFileName().toString();
            return uploadBaseUrl + "/" + uploadFolderName + "/" + relativePath.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new BadRequestException("error.file.upload_failed");
        }
    }

    private void deleteByUrl(String imageUrl) {
        try {
            String uploadFolderName = uploadBasePath.getFileName() == null ? "uploads" : uploadBasePath.getFileName().toString();
            int idx = imageUrl.indexOf("/" + uploadFolderName + "/");
            if (idx < 0) {
                return;
            }

            String relativePart = imageUrl.substring(idx + uploadFolderName.length() + 2);
            Path filePath = uploadBasePath.resolve(relativePart.replace('/', java.io.File.separatorChar)).normalize();
            Files.deleteIfExists(filePath);

            Path dir = filePath.getParent();
            if (dir != null && Files.exists(dir)) {
                try (Stream<Path> walk = Files.list(dir)) {
                    if (walk.findAny().isEmpty()) {
                        Files.deleteIfExists(dir);
                    }
                }
            }
        } catch (IOException ignored) {
        }
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

    private StudentResponse toResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .userId(student.getUserId())
                .email(student.getEmail())
                .fullName(student.getFullName())
                .phone(student.getPhone())
                .bio(student.getBio())
                .skills(student.getSkills())
                .imageFileId(student.getImageFileId())
                .universityId(student.getUniversityId())
                .departmentId(student.getDepartmentId())
                .verificationStatus(student.getVerificationStatus())
                .deleted(student.isDeleted())
                .graduated(student.isGraduated())
                .graduatedAt(student.getGraduatedAt())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
