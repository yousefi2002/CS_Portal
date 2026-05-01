package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateInternshipRequest;
import com.manus.digitalecosystem.dto.request.DeleteInternshipRequest;
import com.manus.digitalecosystem.dto.request.UpdateInternshipRequest;
import com.manus.digitalecosystem.dto.response.InternshipResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Internship;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.InternshipRepository;
import com.manus.digitalecosystem.service.InternshipService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class InternshipServiceImpl implements InternshipService {

    private final InternshipRepository internshipRepository;
    private final CompanyRepository companyRepository;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public InternshipServiceImpl(InternshipRepository internshipRepository,
                                 CompanyRepository companyRepository,
                                 @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                                 @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.internshipRepository = internshipRepository;
        this.companyRepository = companyRepository;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    public InternshipResponse createInternship(CreateInternshipRequest request, MultipartFile image) {
        validateImage(image);
        Company company = findCompanyById(request.getCompanyId());
        ensureCanManageCompany(company);

        Internship internship = Internship.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyDescription(company.getDescription())
                .internshipTitle(request.getTitle())
                .internshipDescription(request.getDescription())
                .requirements(request.getRequirements())
                .roadmap(request.getRoadmap())
                .duration(request.getDuration())
                .requiredSkills(request.getRequiredSkills())
                .applicationRequirements(request.getApplicationRequirements())
                .build();

        Internship saved = internshipRepository.save(internship);
        try {
            saved.setCompanyImage(storeInternshipImage(saved.getId(), image));
            return toResponse(internshipRepository.save(saved));
        } catch (RuntimeException ex) {
            internshipRepository.delete(saved);
            deleteInternshipImageDirectory(saved.getId());
            throw ex;
        }
    }

    @Override
    public InternshipResponse updateInternshipData(String internshipId, UpdateInternshipRequest request) {
        Internship internship = findVisibleInternshipById(internshipId);
        Company company = findCompanyById(internship.getCompanyId());
        ensureCanManageCompany(company);

        internship.setInternshipTitle(request.getTitle());
        internship.setInternshipDescription(request.getDescription());
        internship.setRequirements(request.getRequirements());
        internship.setRoadmap(request.getRoadmap());
        internship.setDuration(request.getDuration());
        internship.setRequiredSkills(request.getRequiredSkills());
        internship.setApplicationRequirements(request.getApplicationRequirements());

        return toResponse(internshipRepository.save(internship));
    }

    @Override
    public InternshipResponse updateInternshipImage(String internshipId, MultipartFile image) {
        validateImage(image);
        Internship internship = findVisibleInternshipById(internshipId);
        Company company = findCompanyById(internship.getCompanyId());
        ensureCanManageCompany(company);

        deleteInternshipImageDirectory(internship.getId());
        internship.setCompanyImage(storeInternshipImage(internship.getId(), image));
        return toResponse(internshipRepository.save(internship));
    }

    @Override
    public List<InternshipResponse> getAllInternships() {
        ensureSuperAdmin();
        return internshipRepository.findByDeletedFalse().stream()
                .sorted(Comparator.comparing(Internship::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InternshipResponse getInternshipById(String internshipId) {
        return toResponse(findVisibleInternshipById(internshipId));
    }

    @Override
    public List<InternshipResponse> searchInternships(String search, String companyId) {
        Pageable pageable = PageRequest.of(0, 100);
        if (StringUtils.hasText(companyId) && StringUtils.hasText(search)) {
            return internshipRepository.findByCompanyIdAndInternshipTitleContainingIgnoreCaseAndDeletedFalse(companyId, search, pageable)
                    .stream().map(this::toResponse).toList();
        }
        if (StringUtils.hasText(companyId)) {
            return internshipRepository.findByCompanyIdAndDeletedFalse(companyId, pageable)
                    .stream().map(this::toResponse).toList();
        }
        if (StringUtils.hasText(search)) {
            return internshipRepository.findByInternshipTitleContainingIgnoreCaseAndDeletedFalse(search, pageable)
                    .stream().map(this::toResponse).toList();
        }
        return internshipRepository.findByDeletedFalse().stream().map(this::toResponse).toList();
    }

    @Override
    public InternshipResponse softDeleteInternship(String internshipId, DeleteInternshipRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getDeleted())) {
            throw new BadRequestException("error.internship.deleted.required");
        }

        Internship internship = findVisibleInternshipById(internshipId);
        Company company = findCompanyById(internship.getCompanyId());
        ensureCanManageCompany(company);

        internship.setDeleted(true);
        return toResponse(internshipRepository.save(internship));
    }

    @Override
    public void deleteInternship(String internshipId) {
        ensureSuperAdmin();
        Internship internship = findInternshipById(internshipId);
        internshipRepository.delete(internship);
        deleteInternshipImageDirectory(internshipId);
    }

    private Internship findVisibleInternshipById(String internshipId) {
        Internship internship = findInternshipById(internshipId);
        if (internship.isDeleted()) {
            throw new ResourceNotFoundException("error.internship.not_found", internshipId);
        }
        return internship;
    }

    private Internship findInternshipById(String internshipId) {
        return internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("error.internship.not_found", internshipId));
    }

    private Company findCompanyById(String companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.company.not_found", companyId));
    }

    private void ensureCanManageCompany(Company company) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }
        if (SecurityUtils.hasRole(Role.COMPANY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(company.getAdminUserId())) {
            return;
        }
        throw new UnauthorizedException("error.company.forbidden");
    }

    private void ensureSuperAdmin() {
        if (!SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new UnauthorizedException("error.auth.forbidden");
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("error.internship.image.required");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("error.internship.image.invalid");
        }
    }

    private String storeInternshipImage(String internshipId, MultipartFile image) {
        Path directory = uploadBasePath.resolve("internship").resolve(internshipId).normalize();
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

    private void deleteInternshipImageDirectory(String internshipId) {
        Path directory = uploadBasePath.resolve("internship").resolve(internshipId).normalize();
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

    private InternshipResponse toResponse(Internship internship) {
        return InternshipResponse.builder()
                .id(internship.getId())
                .companyId(internship.getCompanyId())
                .companyName(internship.getCompanyName())
                .companyDescription(internship.getCompanyDescription())
                .companyImage(internship.getCompanyImage())
                .internshipTitle(internship.getInternshipTitle())
                .internshipDescription(internship.getInternshipDescription())
                .requirements(internship.getRequirements())
                .roadmap(internship.getRoadmap())
                .duration(internship.getDuration())
                .vacancies(internship.getVacancies())
                .requiredSkills(internship.getRequiredSkills())
                .applicationRequirements(internship.getApplicationRequirements())
                .deleted(internship.isDeleted())
                .createdAt(internship.getCreatedAt())
                .updatedAt(internship.getUpdatedAt())
                .build();
    }
}
