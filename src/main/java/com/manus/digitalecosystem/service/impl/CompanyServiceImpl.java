package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.response.CompanyResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.service.CompanyService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
public class CompanyServiceImpl implements CompanyService {

    private static final int MIN_COMPANY_IMAGES = 1;
    private static final int MAX_COMPANY_IMAGES = 5;

    private final CompanyRepository companyRepository;
    private final MongoTemplate mongoTemplate;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public CompanyServiceImpl(CompanyRepository companyRepository,
                              MongoTemplate mongoTemplate,
                              @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                              @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.companyRepository = companyRepository;
        this.mongoTemplate = mongoTemplate;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    public CompanyResponse createCompany(CreateCompanyProfileRequest request, List<MultipartFile> images) {
        ensureSuperAdmin();
        validateCompanyImages(images);

        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .numberOfEmployees(0)
                .developmentType(request.getDevelopmentType())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .adminUserId(request.getAdminUserId())
                .verificationStatus(VerificationStatus.APPROVED)
                .build();

        Company savedCompany = companyRepository.save(company);
        try {
            savedCompany.setImageFileIds(storeCompanyImages(savedCompany.getId(), images));
            return toResponse(companyRepository.save(savedCompany));
        } catch (RuntimeException ex) {
            companyRepository.delete(savedCompany);
            deleteCompanyImageDirectory(savedCompany.getId());
            throw ex;
        }
    }

    @Override
    public CompanyResponse updateCompanyData(String companyId, UpdateCompanyProfileRequest request) {
        Company company = findCompanyById(companyId);
        ensureCanManageCompany(company);

        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setDevelopmentType(request.getDevelopmentType());
        company.setWebsite(request.getWebsite());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        if (request.getAdminUserId() != null) {
            company.setAdminUserId(request.getAdminUserId());
        }

        return toResponse(companyRepository.save(company));
    }

    @Override
    public CompanyResponse updateCompanyImages(String companyId, List<MultipartFile> images) {
        validateCompanyImages(images);

        Company company = findCompanyById(companyId);
        ensureCanManageCompany(company);

        deleteCompanyImageDirectory(company.getId());
        company.setImageFileIds(storeCompanyImages(company.getId(), images));

        return toResponse(companyRepository.save(company));
    }

    @Override
    public List<CompanyResponse> getAllCompanies() {
        ensureSuperAdmin();
        return companyRepository.findAll().stream()
                .sorted(Comparator.comparing(Company::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CompanyResponse getCompanyById(String companyId) {
        return toResponse(findCompanyById(companyId));
    }

    @Override
    public void deleteCompany(String companyId) {
        ensureSuperAdmin();
        Company company = findCompanyById(companyId);
        companyRepository.delete(company);
        deleteCompanyImageDirectory(companyId);
    }

    @Override
    public List<CompanyResponse> searchCompanies(String query) {
        Query mongoQuery = new Query();
        if (StringUtils.hasText(query)) {
            String escaped = java.util.regex.Pattern.quote(query.trim());
            mongoQuery.addCriteria(new Criteria().orOperator(
                    Criteria.where("name.en").regex(escaped, "i"),
                    Criteria.where("name.fa").regex(escaped, "i"),
                    Criteria.where("name.ps").regex(escaped, "i")
            ));
        }
        mongoQuery.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        return mongoTemplate.find(mongoQuery, Company.class).stream()
                .map(this::toResponse)
                .toList();
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

    private void validateCompanyImages(List<MultipartFile> images) {
        if (images == null || images.size() < MIN_COMPANY_IMAGES) {
            throw new BadRequestException("error.company.images.min", MIN_COMPANY_IMAGES);
        }

        if (images.size() > MAX_COMPANY_IMAGES) {
            throw new BadRequestException("error.company.images.max", MAX_COMPANY_IMAGES);
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                throw new BadRequestException("error.company.images.invalid");
            }
            String contentType = image.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new BadRequestException("error.company.images.invalid");
            }
        }
    }

    private List<String> storeCompanyImages(String companyId, List<MultipartFile> images) {
        Path directory = resolveCompanyImageDirectory(companyId);
        try {
            Files.createDirectories(directory);
            List<String> storedPaths = new ArrayList<>();

            for (MultipartFile image : images) {
                String extension = getSafeExtension(image.getOriginalFilename());
                String filename = generateImageName(extension);
                Path target = directory.resolve(filename);
                Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                Path relativePath = uploadBasePath.relativize(target);
                String uploadFolderName = uploadBasePath.getFileName() == null ? "uploads" : uploadBasePath.getFileName().toString();
                String relative = uploadFolderName + "/" + relativePath.toString().replace('\\', '/');
                storedPaths.add(uploadBaseUrl + "/" + relative);
            }

            return storedPaths;
        } catch (IOException ex) {
            throw new BadRequestException("error.file.upload_failed");
        }
    }

    private void deleteCompanyImageDirectory(String companyId) {
        Path directory = resolveCompanyImageDirectory(companyId);
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

    private Path resolveCompanyImageDirectory(String companyId) {
        return uploadBasePath.resolve("company").resolve(companyId).normalize();
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

    private CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .numberOfEmployees(company.getNumberOfEmployees())
                .developmentType(company.getDevelopmentType())
                .website(company.getWebsite())
                .phone(company.getPhone())
                .email(company.getEmail())
                .imageFileIds(company.getImageFileIds())
                .adminUserId(company.getAdminUserId())
                .verificationStatus(company.getVerificationStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
