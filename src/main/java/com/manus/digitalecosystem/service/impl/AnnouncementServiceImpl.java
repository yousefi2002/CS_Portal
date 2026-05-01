package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.DeleteAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAnnouncementRequest;
import com.manus.digitalecosystem.dto.response.AnnouncementResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.exception.UnauthorizedException;
import com.manus.digitalecosystem.model.Announcement;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.repository.AnnouncementRepository;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.AnnouncementService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final MongoTemplate mongoTemplate;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                   UniversityRepository universityRepository,
                                   DepartmentRepository departmentRepository,
                                   MongoTemplate mongoTemplate) {
        this.announcementRepository = announcementRepository;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request) {
        if (request == null) {
            throw new BadRequestException("error.announcement.data.required");
        }

        Scope scope = resolveAndAuthorizeScope(request.getUniversityId(), request.getDepartmentId());

        Announcement announcement = Announcement.builder()
                .universityId(scope.universityId())
                .departmentId(scope.departmentId())
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .createdByUserId(SecurityUtils.getCurrentUserId())
                .build();

        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    public AnnouncementResponse updateAnnouncement(String announcementId, UpdateAnnouncementRequest request) {
        Announcement announcement = findVisibleAnnouncementById(announcementId);
        ensureCanManageAnnouncement(announcement);

        Scope scope = resolveAndAuthorizeScope(request.getUniversityId(), request.getDepartmentId());

        announcement.setUniversityId(scope.universityId());
        announcement.setDepartmentId(scope.departmentId());
        announcement.setCategory(request.getCategory());
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());

        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    public AnnouncementResponse softDeleteAnnouncement(String announcementId, DeleteAnnouncementRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getDeleted())) {
            throw new BadRequestException("error.announcement.deleted.required");
        }

        Announcement announcement = findVisibleAnnouncementById(announcementId);
        ensureCanManageAnnouncement(announcement);

        announcement.setDeleted(true);
        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    public void deleteAnnouncement(String announcementId) {
        ensureSuperAdmin();
        Announcement announcement = findAnnouncementById(announcementId);
        announcementRepository.delete(announcement);
    }

    @Override
    public List<AnnouncementResponse> searchAnnouncements(String query, String universityId, String departmentId) {
        Query mongoQuery = new Query();

        if (!SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            mongoQuery.addCriteria(Criteria.where("deleted").is(false));
        }

        Scope requestedScope = resolveRequestedScope(universityId, departmentId);
        Scope effectiveScope = resolveEffectiveScopeForRead(requestedScope);

        if (StringUtils.hasText(effectiveScope.universityId())) {
            mongoQuery.addCriteria(Criteria.where("universityId").is(effectiveScope.universityId()));
        }
        if (StringUtils.hasText(effectiveScope.departmentId())) {
            mongoQuery.addCriteria(Criteria.where("departmentId").is(effectiveScope.departmentId()));
        }

        if (StringUtils.hasText(query)) {
            String escaped = Pattern.quote(query.trim());
            mongoQuery.addCriteria(new Criteria().orOperator(
                    Criteria.where("title.en").regex(escaped, "i"),
                    Criteria.where("title.fa").regex(escaped, "i"),
                    Criteria.where("title.ps").regex(escaped, "i"),
                    Criteria.where("content.en").regex(escaped, "i"),
                    Criteria.where("content.fa").regex(escaped, "i"),
                    Criteria.where("content.ps").regex(escaped, "i")
            ));
        }

        mongoQuery.limit(200);

        return mongoTemplate.find(mongoQuery, Announcement.class).stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AnnouncementResponse> getAllAnnouncements() {
        ensureSuperAdmin();
        return announcementRepository.findAll(Pageable.unpaged()).stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AnnouncementResponse getAnnouncementById(String announcementId) {
        Announcement announcement = findAnnouncementById(announcementId);
        ensureCanViewAnnouncement(announcement);
        if (announcement.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new ResourceNotFoundException("error.announcement.not_found", announcementId);
        }
        return toResponse(announcement);
    }

    @Override
    public List<AnnouncementResponse> getAnnouncementsByUniversity(String universityId) {
        University university = findUniversityById(universityId);
        ensureCanAccessUniversity(university);

        List<Announcement> announcements = SecurityUtils.hasRole(Role.SUPER_ADMIN.name())
                ? announcementRepository.findByUniversityIdAndDepartmentIdIsNull(universityId, Pageable.unpaged())
                : announcementRepository.findByUniversityIdAndDepartmentIdIsNullAndDeletedFalse(universityId, Pageable.unpaged());

        return announcements.stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AnnouncementResponse> getAnnouncementsByDepartment(String departmentId) {
        Department department = findDepartmentById(departmentId);
        ensureCanAccessDepartment(department);

        List<Announcement> announcements = SecurityUtils.hasRole(Role.SUPER_ADMIN.name())
                ? announcementRepository.findByDepartmentId(departmentId, Pageable.unpaged())
                : announcementRepository.findByDepartmentIdAndDeletedFalse(departmentId, Pageable.unpaged());

        return announcements.stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    private Scope resolveAndAuthorizeScope(String universityId, String departmentId) {
        Scope scope = resolveRequestedScope(universityId, departmentId);

        if (!StringUtils.hasText(scope.universityId()) && !StringUtils.hasText(scope.departmentId())) {
            throw new BadRequestException("error.announcement.scope.required");
        }

        if (StringUtils.hasText(scope.departmentId())) {
            Department department = findDepartmentById(scope.departmentId());
            if (department.isDeleted() && !SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
                throw new ResourceNotFoundException("error.department.not_found", scope.departmentId());
            }
            ensureCanAccessDepartment(department);
            return new Scope(department.getUniversityId(), department.getId());
        }

        University university = findUniversityById(scope.universityId());
        ensureCanAccessUniversity(university);
        return new Scope(university.getId(), null);
    }

    private Scope resolveRequestedScope(String universityId, String departmentId) {
        String normalizedUniversityId = StringUtils.hasText(universityId) ? universityId.trim() : null;
        String normalizedDepartmentId = StringUtils.hasText(departmentId) ? departmentId.trim() : null;

        if (StringUtils.hasText(normalizedUniversityId) && StringUtils.hasText(normalizedDepartmentId)) {
            throw new BadRequestException("error.announcement.scope.invalid");
        }

        return new Scope(normalizedUniversityId, normalizedDepartmentId);
    }

    private Scope resolveEffectiveScopeForRead(Scope requestedScope) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return requestedScope;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityByAdminUserId(SecurityUtils.getCurrentUserId());
            if (StringUtils.hasText(requestedScope.departmentId())) {
                Department department = findDepartmentById(requestedScope.departmentId());
                if (!university.getId().equals(department.getUniversityId())) {
                    throw new UnauthorizedException("error.announcement.forbidden");
                }
                return new Scope(university.getId(), department.getId());
            }

            if (StringUtils.hasText(requestedScope.universityId()) && !university.getId().equals(requestedScope.universityId())) {
                throw new UnauthorizedException("error.announcement.forbidden");
            }

            return new Scope(university.getId(), null);
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            Department department = findDepartmentByAdminUserId(SecurityUtils.getCurrentUserId());

            if (StringUtils.hasText(requestedScope.departmentId()) && !department.getId().equals(requestedScope.departmentId())) {
                throw new UnauthorizedException("error.announcement.forbidden");
            }

            if (StringUtils.hasText(requestedScope.universityId()) && !department.getUniversityId().equals(requestedScope.universityId())) {
                throw new UnauthorizedException("error.announcement.forbidden");
            }

            if (StringUtils.hasText(requestedScope.departmentId())) {
                return new Scope(department.getUniversityId(), department.getId());
            }

            return new Scope(department.getUniversityId(), null);
        }

        throw new UnauthorizedException("error.auth.forbidden");
    }

    private Announcement findVisibleAnnouncementById(String announcementId) {
        Announcement announcement = findAnnouncementById(announcementId);
        if (announcement.isDeleted()) {
            throw new ResourceNotFoundException("error.announcement.not_found", announcementId);
        }
        return announcement;
    }

    private Announcement findAnnouncementById(String announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("error.announcement.not_found", announcementId));
    }

    private University findUniversityById(String universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.not_found", universityId));
    }

    private Department findDepartmentById(String departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));
    }

    private University findUniversityByAdminUserId(String userId) {
        return universityRepository.findByAdminUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found", userId));
    }

    private Department findDepartmentByAdminUserId(String userId) {
        return departmentRepository.findByAdminUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found", userId));
    }

    private void ensureCanManageAnnouncement(Announcement announcement) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityByAdminUserId(SecurityUtils.getCurrentUserId());
            if (university.getId().equals(announcement.getUniversityId())) {
                return;
            }
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            Department department = findDepartmentByAdminUserId(SecurityUtils.getCurrentUserId());
            if (department.getId().equals(announcement.getDepartmentId())) {
                return;
            }
        }

        throw new UnauthorizedException("error.announcement.forbidden");
    }

    private void ensureCanViewAnnouncement(Announcement announcement) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityByAdminUserId(SecurityUtils.getCurrentUserId());
            if (university.getId().equals(announcement.getUniversityId())) {
                return;
            }
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            Department department = findDepartmentByAdminUserId(SecurityUtils.getCurrentUserId());
            boolean canViewDepartmentAnnouncement = department.getId().equals(announcement.getDepartmentId());
            boolean canViewUniversityAnnouncement = !StringUtils.hasText(announcement.getDepartmentId())
                    && department.getUniversityId().equals(announcement.getUniversityId());

            if (canViewDepartmentAnnouncement || canViewUniversityAnnouncement) {
                return;
            }
        }

        throw new UnauthorizedException("error.announcement.forbidden");
    }

    private void ensureCanAccessUniversity(University university) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(university.getAdminUserId())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name())) {
            Department department = findDepartmentByAdminUserId(SecurityUtils.getCurrentUserId());
            if (department.getUniversityId().equals(university.getId())) {
                return;
            }
        }

        throw new UnauthorizedException("error.announcement.forbidden");
    }

    private void ensureCanAccessDepartment(Department department) {
        if (SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.UNIVERSITY_ADMIN.name())) {
            University university = findUniversityByAdminUserId(SecurityUtils.getCurrentUserId());
            if (university.getId().equals(department.getUniversityId())) {
                return;
            }
        }

        if (SecurityUtils.hasRole(Role.DEPARTMENT_ADMIN.name()) && SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
            return;
        }

        throw new UnauthorizedException("error.announcement.forbidden");
    }

    private void ensureSuperAdmin() {
        if (!SecurityUtils.hasRole(Role.SUPER_ADMIN.name())) {
            throw new UnauthorizedException("error.auth.forbidden");
        }
    }

    private AnnouncementResponse toResponse(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .universityId(announcement.getUniversityId())
                .departmentId(announcement.getDepartmentId())
                .category(announcement.getCategory())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .createdByUserId(announcement.getCreatedByUserId())
                .deleted(announcement.isDeleted())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }

    private record Scope(String universityId, String departmentId) {
    }
}
