package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateUniversityProfileRequest;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;
    private final MongoTemplate mongoTemplate;

    public UniversityServiceImpl(UniversityRepository universityRepository, MongoTemplate mongoTemplate) {
        this.universityRepository = universityRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UniversityResponse createUniversity(CreateUniversityProfileRequest request) {
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
                .imageFileId(request.getImageFileId())
                .adminUserId(request.getAdminUserId())
                .verificationStatus(VerificationStatus.APPROVED)
                .build();

        return UniversityMapper.toResponse(universityRepository.save(university));
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
        university.setImageFileId(request.getImageFileId());

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
}