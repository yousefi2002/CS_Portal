package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateAnnouncementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAnnouncementRequest;
import com.manus.digitalecosystem.dto.response.AnnouncementResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Announcement;
import com.manus.digitalecosystem.model.AnnouncementCategory;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.NotificationType;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.repository.AnnouncementRepository;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.AnnouncementService;
import com.manus.digitalecosystem.service.NotificationService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final MongoTemplate mongoTemplate;

    public AnnouncementServiceImpl(
            AnnouncementRepository announcementRepository,
            UniversityRepository universityRepository,
            DepartmentRepository departmentRepository,
            StudentRepository studentRepository,
            NotificationService notificationService,
            MongoTemplate mongoTemplate
    ) {
        this.announcementRepository = announcementRepository;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        Announcement announcement = Announcement.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .createdByUserId(currentUserId)
                .build();

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            if (request.getUniversityId() == null || request.getUniversityId().isBlank()) {
                throw new BadRequestException("error.announcement.university.required");
            }
            announcement.setUniversityId(request.getUniversityId());
            if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
                Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", request.getDepartmentId()));
                if (!department.getUniversityId().equals(request.getUniversityId())) {
                    throw new BadRequestException("error.department.university_mismatch");
                }
                announcement.setDepartmentId(department.getId());
            }
        } else if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            announcement.setUniversityId(myUniversityId);

            if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
                Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", request.getDepartmentId()));
                if (!myUniversityId.equals(department.getUniversityId())) {
                    throw new AccessDeniedException("Forbidden");
                }
                announcement.setDepartmentId(department.getId());
            }
        } else if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = getMyDepartmentOrThrow();
            announcement.setUniversityId(myDepartment.getUniversityId());
            announcement.setDepartmentId(myDepartment.getId());
        } else {
            throw new AccessDeniedException("Forbidden");
        }

        Announcement saved = announcementRepository.save(announcement);
        notifyAnnouncement(saved, currentUserId);
        return AnnouncementResponse.fromAnnouncement(saved);
    }

    @Override
    public AnnouncementResponse updateAnnouncement(String id, UpdateAnnouncementRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.announcement.not_found", id));

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            if (request.getUniversityId() != null && !request.getUniversityId().isBlank()) {
                announcement.setUniversityId(request.getUniversityId());
            }
            if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
                Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", request.getDepartmentId()));
                if (announcement.getUniversityId() != null && !announcement.getUniversityId().equals(department.getUniversityId())) {
                    throw new BadRequestException("error.department.university_mismatch");
                }
                announcement.setDepartmentId(department.getId());
            } else {
                announcement.setDepartmentId(null);
            }
        } else if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            if (!myUniversityId.equals(announcement.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            boolean canEdit = currentUserId.equals(announcement.getCreatedByUserId())
                    || announcement.getDepartmentId() == null;
            if (!canEdit) {
                throw new AccessDeniedException("Forbidden");
            }
        } else if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = getMyDepartmentOrThrow();
            if (!myDepartment.getId().equals(announcement.getDepartmentId())
                    || !currentUserId.equals(announcement.getCreatedByUserId())) {
                throw new AccessDeniedException("Forbidden");
            }
        } else {
            throw new AccessDeniedException("Forbidden");
        }

        announcement.setCategory(request.getCategory());
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());

        return AnnouncementResponse.fromAnnouncement(announcementRepository.save(announcement));
    }

    @Override
    public void deleteAnnouncement(String id) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.announcement.not_found", id));

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            announcementRepository.deleteById(id);
            return;
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            if (!myUniversityId.equals(announcement.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            boolean canDelete = currentUserId.equals(announcement.getCreatedByUserId())
                    || announcement.getDepartmentId() == null;
            if (!canDelete) {
                throw new AccessDeniedException("Forbidden");
            }
            announcementRepository.deleteById(id);
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = getMyDepartmentOrThrow();
            if (!myDepartment.getId().equals(announcement.getDepartmentId())
                    || !currentUserId.equals(announcement.getCreatedByUserId())) {
                throw new AccessDeniedException("Forbidden");
            }
            announcementRepository.deleteById(id);
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    @Override
    public AnnouncementResponse getAnnouncementById(String id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.announcement.not_found", id));

        enforceViewScope(announcement);
        return AnnouncementResponse.fromAnnouncement(announcement);
    }

    @Override
    public PagedResponse<AnnouncementResponse> searchAnnouncements(
            String q,
            AnnouncementCategory category,
            String universityId,
            String departmentId,
            Pageable pageable
    ) {
        List<Criteria> criteriaList = new ArrayList<>();
        applyViewScopeCriteria(criteriaList, universityId, departmentId);

        if (category != null) {
            criteriaList.add(Criteria.where("category").is(category));
        }
        if (q != null && !q.isBlank()) {
            criteriaList.add(Criteria.where("title").regex(Pattern.quote(q), "i"));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria).with(pageable);
        List<Announcement> announcements = mongoTemplate.find(query, Announcement.class);
        long total = mongoTemplate.count(new Query(criteria), Announcement.class);

        Page<Announcement> page = new PageImpl<>(announcements, pageable, total);
        return PagedResponse.fromPage(page.map(AnnouncementResponse::fromAnnouncement));
    }

    private void enforceViewScope(Announcement announcement) {
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return;
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            if (!myUniversityId.equals(announcement.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = getMyDepartmentOrThrow();
            if (!myDepartment.getUniversityId().equals(announcement.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            if (announcement.getDepartmentId() != null && !myDepartment.getId().equals(announcement.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("STUDENT")) {
            Student student = getMyStudentOrThrow();
            if (!student.getUniversityId().equals(announcement.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            if (announcement.getDepartmentId() != null && !announcement.getDepartmentId().equals(student.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("COMPANY_ADMIN")) {
            if (announcement.getDepartmentId() != null) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    private void applyViewScopeCriteria(List<Criteria> criteriaList, String universityId, String departmentId) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            if (universityId != null && !universityId.isBlank()) {
                criteriaList.add(Criteria.where("universityId").is(universityId));
            }
            if (departmentId != null && !departmentId.isBlank()) {
                criteriaList.add(Criteria.where("departmentId").is(departmentId));
            }
            return;
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            criteriaList.add(Criteria.where("universityId").is(myUniversityId));
            if (departmentId != null && !departmentId.isBlank()) {
                Department department = departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));
                if (!myUniversityId.equals(department.getUniversityId())) {
                    throw new AccessDeniedException("Forbidden");
                }
                criteriaList.add(Criteria.where("departmentId").is(departmentId));
            }
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = getMyDepartmentOrThrow();
            criteriaList.add(Criteria.where("universityId").is(myDepartment.getUniversityId()));
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("departmentId").is(null),
                    Criteria.where("departmentId").is(myDepartment.getId())
            ));
            return;
        }

        if (SecurityUtils.hasRole("STUDENT")) {
            Student student = getMyStudentOrThrow();
            criteriaList.add(Criteria.where("universityId").is(student.getUniversityId()));
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("departmentId").is(null),
                    Criteria.where("departmentId").is(student.getDepartmentId())
            ));
            return;
        }

        if (SecurityUtils.hasRole("COMPANY_ADMIN")) {
            criteriaList.add(Criteria.where("departmentId").is(null));
            if (universityId != null && !universityId.isBlank()) {
                criteriaList.add(Criteria.where("universityId").is(universityId));
            }
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    private String getMyUniversityIdOrThrow() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return universityRepository.findByAdminUserId(currentUserId)
                .map(University::getId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
    }

    private Department getMyDepartmentOrThrow() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return departmentRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
    }

    private Student getMyStudentOrThrow() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return studentRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));
    }

    private void notifyAnnouncement(Announcement announcement, String actorUserId) {
        List<Student> students;
        if (announcement.getDepartmentId() != null && !announcement.getDepartmentId().isBlank()) {
            students = studentRepository.findByDepartmentId(announcement.getDepartmentId());
        } else {
            students = studentRepository.findByUniversityId(announcement.getUniversityId());
        }

        for (Student student : students) {
            if (student.getUserId() == null || student.getUserId().isBlank()) {
                continue;
            }
            if (student.getUserId().equals(actorUserId)) {
                continue;
            }
            notificationService.createNotification(
                    student.getUserId(),
                    NotificationType.UNIVERSITY_ANNOUNCEMENT,
                    "notification.announcement.new.title",
                    "notification.announcement.new.body",
                    List.of(announcement.getTitle())
            );
        }
    }
}
