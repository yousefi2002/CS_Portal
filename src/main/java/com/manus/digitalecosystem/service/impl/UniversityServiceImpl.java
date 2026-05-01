package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.UniversityVisibility;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.security.UserDetailsImpl;
import com.manus.digitalecosystem.service.UniversityService;
import com.manus.digitalecosystem.service.mapper.UniversityMapper;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class UniversityServiceImpl implements UniversityService {

    private static final int MIN_UNIVERSITY_IMAGES = 1;
    private static final int MAX_UNIVERSITY_IMAGES = 5;

    private final UniversityRepository universityRepository;
    private final MongoTemplate mongoTemplate;
    private final com.manus.digitalecosystem.repository.StudentRepository studentRepository;
    private final com.manus.digitalecosystem.repository.AchievementRepository achievementRepository;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public UniversityServiceImpl(UniversityRepository universityRepository,
                                 MongoTemplate mongoTemplate,
                                 com.manus.digitalecosystem.repository.StudentRepository studentRepository,
                                 com.manus.digitalecosystem.repository.AchievementRepository achievementRepository,
                                 @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                                 @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.universityRepository = universityRepository;
        this.mongoTemplate = mongoTemplate;
        this.studentRepository = studentRepository;
        this.achievementRepository = achievementRepository;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    public UniversityResponse createUniversity(CreateUniversityProfileRequest request, List<MultipartFile> images) {
        if (request == null) {
            throw new BadRequestException("error.university.data.required");
        }

        validateUniversityImages(images);

        if (universityRepository.findByAdminUserId(request.getAdminUserId()).isPresent()) {
            throw new DuplicateResourceException("error.university.profile.exists");
        }

        University university = University.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .visibility(request.getVisibility())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .adminUserId(request.getAdminUserId())
                .verificationStatus(VerificationStatus.APPROVED)
                .build();

        University savedUniversity = universityRepository.save(university);
        try {
            savedUniversity.setImageFileIds(storeUniversityImages(savedUniversity.getId(), images));
            return UniversityMapper.toResponse(universityRepository.save(savedUniversity));
        } catch (RuntimeException ex) {
            System.out.println("Error occurred: " + ex.getMessage());
            universityRepository.delete(savedUniversity);
            throw ex;
        }
    }

    @Override
    public UniversityResponse updateUniversity(String universityId, UpdateUniversityProfileRequest request) {
        University university = findUniversityById(universityId);
        ensureCanManageUniversity(university);

        university.setName(request.getName());
        university.setDescription(request.getDescription());
        university.setAddress(request.getAddress());
        university.setVisibility(request.getVisibility());
        university.setWebsite(request.getWebsite());
        university.setPhone(request.getPhone());
        university.setEmail(request.getEmail());

        return UniversityMapper.toResponse(universityRepository.save(university));
    }

    @Override
    public UniversityResponse updateUniversityImages(String universityId, List<MultipartFile> images) {
        validateUniversityImages(images);

        University university = findUniversityById(universityId);
        ensureCanManageUniversity(university);

        deleteUniversityImageDirectory(university.getId());
        university.setImageFileIds(storeUniversityImages(university.getId(), images));

        return UniversityMapper.toResponse(universityRepository.save(university));
    }

    @Override
    public Page<UniversityResponse> getAllUniversities(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return universityRepository.findAll(pageRequest)
                .map(UniversityMapper::toResponse);
    }

    @Override
    public UniversityResponse getUniversityById(String universityId) {
        return UniversityMapper.toResponse(findUniversityById(universityId));
    }

    @Override
    public void deleteUniversity(String universityId) {
        University university = findUniversityById(universityId);
        universityRepository.delete(university);
        deleteUniversityImageDirectory(universityId);
    }

    @Override
    public Page<UniversityResponse> searchUniversities(String search,
                                                       UniversityVisibility visibility,
                                                       String rank,
                                                       int page,
                                                       int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Query query = new Query();
        if (visibility != null) {
            query.addCriteria(Criteria.where("visibility").is(visibility));
        }

        String normalizedSearch = normalize(search);
        if (StringUtils.hasText(normalizedSearch)) {
            String escapedSearch = Pattern.quote(normalizedSearch);
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name.en").regex(escapedSearch, "i"),
                    Criteria.where("name.fa").regex(escapedSearch, "i"),
                    Criteria.where("name.ps").regex(escapedSearch, "i")
            ));
        }

        List<University> universities = mongoTemplate.find(query, University.class);
        Comparator<University> comparator = Comparator
                .comparingInt((University university) -> score(university.getName(), normalizedSearch))
                .reversed()
                .thenComparing(University::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));

        if ("asc".equalsIgnoreCase(rank)) {
            comparator = comparator.reversed();
        }

        universities.sort(comparator);

        int fromIndex = Math.min(safePage * safeSize, universities.size());
        int toIndex = Math.min(fromIndex + safeSize, universities.size());
        List<UniversityResponse> pageContent = universities.subList(fromIndex, toIndex).stream()
            .map(UniversityMapper::toResponse)
            .toList();

        return new PageImpl<>(pageContent, PageRequest.of(safePage, safeSize), universities.size());
    }

    @Override
    public java.util.List<com.manus.digitalecosystem.dto.response.TopStudentResponse> getTopStudents(String universityId, int limit) {
        var university = findUniversityById(universityId);
        ensureCanViewUniversity(university);

        // collect students in university (hide soft-deleted students for non-super-admin callers)
        var students = com.manus.digitalecosystem.util.SecurityUtils.hasRole(com.manus.digitalecosystem.model.enums.Role.SUPER_ADMIN.name())
            ? studentRepository.findByUniversityId(universityId)
            : studentRepository.findByUniversityIdAndDeletedFalse(universityId);

        // compute achievement counts per student using achievementRepository count
        var counts = new java.util.ArrayList<com.manus.digitalecosystem.dto.response.TopStudentResponse>();
        for (var student : students) {
            long cnt = achievementRepository.countByContributorsStudentIdAndDeletedFalse(student.getId());
            if (cnt <= 0) continue;
            counts.add(com.manus.digitalecosystem.dto.response.TopStudentResponse.builder()
                    .studentId(student.getId())
                    .fullName(student.getFullName())
                    .email(student.getEmail())
                    .imageFileId(student.getImageFileId())
                    .achievementCount(cnt)
                    .build());
        }

        return counts.stream()
                .sorted(java.util.Comparator.comparingLong(com.manus.digitalecosystem.dto.response.TopStudentResponse::getAchievementCount).reversed())
                .limit(Math.max(1, limit))
                .toList();
    }

    private University findUniversityById(String universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", universityId));
    }

    private void ensureCanManageUniversity(University university) {
        UserDetailsImpl currentUser = SecurityUtils.getCurrentUserDetails();
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) && currentUser.getId().equals(university.getAdminUserId())) {
            return;
        }

        throw new UnauthorizedException("error.university.forbidden");
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

    private int score(com.manus.digitalecosystem.model.LocalizedText text, String search) {
        if (!StringUtils.hasText(search) || text == null) {
            return 0;
        }

        int best = 0;
        best = Math.max(best, scoreValue(text.getEn(), search));
        best = Math.max(best, scoreValue(text.getFa(), search));
        best = Math.max(best, scoreValue(text.getPs(), search));
        return best;
    }

    private int scoreValue(String candidate, String search) {
        if (!StringUtils.hasText(candidate) || !StringUtils.hasText(search)) {
            return 0;
        }

        String normalizedCandidate = normalize(candidate);
        if (normalizedCandidate.equals(search)) {
            return 300;
        }
        if (normalizedCandidate.startsWith(search)) {
            return 200;
        }
        if (normalizedCandidate.contains(search)) {
            return 100;
        }
        return 0;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void validateUniversityImages(List<MultipartFile> images) {
        if (images == null || images.size() < MIN_UNIVERSITY_IMAGES) {
            throw new BadRequestException("error.university.images.min", MIN_UNIVERSITY_IMAGES);
        }

        if (images.size() > MAX_UNIVERSITY_IMAGES) {
            throw new BadRequestException("error.university.images.max", MAX_UNIVERSITY_IMAGES);
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                throw new BadRequestException("error.university.images.invalid");
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new BadRequestException("error.university.images.invalid");
            }
        }
    }

    private List<String> storeUniversityImages(String universityId, List<MultipartFile> images) {
        Path directory = resolveUniversityImageDirectory(universityId);
        try {
            Files.createDirectories(directory);
            List<String> storedPaths = new ArrayList<>();

            for (MultipartFile image : images) {
                String extension = getSafeExtension(image.getOriginalFilename());
                String filename = generateShortImageName(extension);
                Path target = directory.resolve(filename);
                Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                Path relativePath = uploadBasePath.relativize(target);
                String uploadFolderName = uploadBasePath.getFileName() == null
                    ? "uploads"
                    : uploadBasePath.getFileName().toString();
                String relative = uploadFolderName + "/" + relativePath.toString().replace('\\', '/');
                String fullUrl = uploadBaseUrl + "/" + relative;
                storedPaths.add(fullUrl);
            }

            return storedPaths;
        } catch (IOException ex) {
            throw new BadRequestException("error.file.upload_failed");
        }
    }

    private void deleteUniversityImageDirectory(String universityId) {
        Path directory = resolveUniversityImageDirectory(universityId);
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

    private Path resolveUniversityImageDirectory(String universityId) {
        return uploadBasePath.resolve("university").resolve(universityId).normalize();
    }

    private String getSafeExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return ".bin";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == originalFilename.length() - 1) {
            return ".bin";
        }

        String extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (!extension.matches("\\.[a-z0-9]{1,8}")) {
            return ".bin";
        }

        return extension;
    }

    private String generateShortImageName(String extension) {
        String id = UUID.randomUUID().toString().replace("-", "");
        String shortId = id.length() > 8 ? id.substring(0, 8) : id;
        return "im" + shortId + extension;
    }
}