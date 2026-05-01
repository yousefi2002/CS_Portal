package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateJobRequest;
import com.manus.digitalecosystem.dto.request.DeleteJobRequest;
import com.manus.digitalecosystem.dto.request.UpdateJobRequest;
import com.manus.digitalecosystem.dto.response.JobResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.Job;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.JobRepository;
import com.manus.digitalecosystem.service.JobService;
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
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final Path uploadBasePath;
    private final String uploadBaseUrl;

    public JobServiceImpl(JobRepository jobRepository,
                          CompanyRepository companyRepository,
                          @Value("${app.upload.base-path:uploads}") String uploadBasePath,
                          @Value("${app.upload.base-url:http://localhost:8080}") String uploadBaseUrl) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl.replaceAll("/+$", "") : "http://localhost:8080";
    }

    @Override
    public JobResponse createJob(CreateJobRequest request, MultipartFile image) {
        validateImage(image);
        Company company = findCompanyById(request.getCompanyId());
        ensureCanManageCompany(company);

        Job job = Job.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyDescription(company.getDescription())
                .jobTitle(request.getTitle())
                .jobDescription(request.getDescription())
            .requirement(request.getRequirements() == null ? null : List.of(request.getRequirements()))
                .location(request.getLocation())
                .requiredSkills(request.getRequiredSkills())
                .build();

        Job saved = jobRepository.save(job);
        try {
            saved.setCompanyImage(storeJobImage(saved.getId(), image));
            return toResponse(jobRepository.save(saved));
        } catch (RuntimeException ex) {
            jobRepository.delete(saved);
            deleteJobImageDirectory(saved.getId());
            throw ex;
        }
    }

    @Override
    public JobResponse updateJobData(String jobId, UpdateJobRequest request) {
        Job job = findVisibleJobById(jobId);
        Company company = findCompanyById(job.getCompanyId());
        ensureCanManageCompany(company);

        job.setJobTitle(request.getTitle());
        job.setJobDescription(request.getDescription());
        job.setRequirement(request.getRequirements() == null ? null : List.of(request.getRequirements()));
        job.setLocation(request.getLocation());
        job.setRequiredSkills(request.getRequiredSkills());

        return toResponse(jobRepository.save(job));
    }

    @Override
    public JobResponse updateJobImage(String jobId, MultipartFile image) {
        validateImage(image);
        Job job = findVisibleJobById(jobId);
        Company company = findCompanyById(job.getCompanyId());
        ensureCanManageCompany(company);

        deleteJobImageDirectory(job.getId());
        job.setCompanyImage(storeJobImage(job.getId(), image));
        return toResponse(jobRepository.save(job));
    }

    @Override
    public List<JobResponse> getAllJobs() {
        ensureSuperAdmin();
        return jobRepository.findByDeletedFalse().stream()
                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public JobResponse getJobById(String jobId) {
        return toResponse(findVisibleJobById(jobId));
    }

    @Override
    public List<JobResponse> searchJobs(String search, String companyId) {
        Pageable pageable = PageRequest.of(0, 100);
        if (StringUtils.hasText(companyId) && StringUtils.hasText(search)) {
            return jobRepository.findByCompanyIdAndJobTitleContainingIgnoreCaseAndDeletedFalse(companyId, search, pageable)
                    .stream().map(this::toResponse).toList();
        }
        if (StringUtils.hasText(companyId)) {
            return jobRepository.findByCompanyIdAndDeletedFalse(companyId, pageable)
                    .stream().map(this::toResponse).toList();
        }
        if (StringUtils.hasText(search)) {
            return jobRepository.findByJobTitleContainingIgnoreCaseAndDeletedFalse(search, pageable)
                    .stream().map(this::toResponse).toList();
        }
        return jobRepository.findByDeletedFalse().stream().map(this::toResponse).toList();
    }

    @Override
    public JobResponse softDeleteJob(String jobId, DeleteJobRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getDeleted())) {
            throw new BadRequestException("error.job.deleted.required");
        }

        Job job = findVisibleJobById(jobId);
        Company company = findCompanyById(job.getCompanyId());
        ensureCanManageCompany(company);

        job.setDeleted(true);
        return toResponse(jobRepository.save(job));
    }

    @Override
    public void deleteJob(String jobId) {
        ensureSuperAdmin();
        Job job = findJobById(jobId);
        jobRepository.delete(job);
        deleteJobImageDirectory(jobId);
    }

    private Job findVisibleJobById(String jobId) {
        Job job = findJobById(jobId);
        if (job.isDeleted()) {
            throw new ResourceNotFoundException("error.job.not_found", jobId);
        }
        return job;
    }

    private Job findJobById(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("error.job.not_found", jobId));
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
            throw new BadRequestException("error.job.image.required");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("error.job.image.invalid");
        }
    }

    private String storeJobImage(String jobId, MultipartFile image) {
        Path directory = uploadBasePath.resolve("job").resolve(jobId).normalize();
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

    private void deleteJobImageDirectory(String jobId) {
        Path directory = uploadBasePath.resolve("job").resolve(jobId).normalize();
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

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .companyName(job.getCompanyName())
                .companyDescription(job.getCompanyDescription())
                .companyImage(job.getCompanyImage())
                .jobTitle(job.getJobTitle())
                .jobDescription(job.getJobDescription())
                .salary(job.getSalary())
                .type(job.getType())
                .vacancies(job.getVacancies())
                .requirement(job.getRequirement())
                .location(job.getLocation())
                .requiredSkills(job.getRequiredSkills())
                .deleted(job.isDeleted())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
