package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateAchievementRequest;
import com.manus.digitalecosystem.dto.request.UpdateAchievementRequest;
import com.manus.digitalecosystem.dto.response.AchievementResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Achievement;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.repository.AchievementRepository;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.AchievementService;
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
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UniversityRepository universityRepository;
    private final MongoTemplate mongoTemplate;

    public AchievementServiceImpl(
            AchievementRepository achievementRepository,
            StudentRepository studentRepository,
            DepartmentRepository departmentRepository,
            UniversityRepository universityRepository,
            MongoTemplate mongoTemplate
    ) {
        this.achievementRepository = achievementRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.universityRepository = universityRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public AchievementResponse createAchievement(CreateAchievementRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", request.getStudentId()));

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            // allowed
        } else if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            if (!myUniversityId.equals(student.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
        } else if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            String myDepartmentId = getMyDepartmentIdOrThrow();
            if (!myDepartmentId.equals(student.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
        } else {
            throw new AccessDeniedException("Forbidden");
        }

        Achievement achievement = Achievement.builder()
                .studentId(student.getId())
                .universityId(student.getUniversityId())
                .departmentId(student.getDepartmentId())
                .title(request.getTitle())
                .description(request.getDescription())
                .achievedAt(request.getAchievedAt())
                .createdByUserId(currentUserId)
                .build();

        return AchievementResponse.fromAchievement(achievementRepository.save(achievement));
    }

    @Override
    public AchievementResponse updateAchievement(String id, UpdateAchievementRequest request) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.achievement.not_found", id));

        enforceManageScope(achievement);

        achievement.setTitle(request.getTitle());
        achievement.setDescription(request.getDescription());
        achievement.setAchievedAt(request.getAchievedAt());

        return AchievementResponse.fromAchievement(achievementRepository.save(achievement));
    }

    @Override
    public void deleteAchievement(String id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.achievement.not_found", id));

        enforceManageScope(achievement);
        achievementRepository.deleteById(id);
    }

    @Override
    public AchievementResponse getAchievementById(String id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.achievement.not_found", id));

        enforceViewScope(achievement);
        return AchievementResponse.fromAchievement(achievement);
    }

    @Override
    public PagedResponse<AchievementResponse> searchAchievements(String q, String studentId, String universityId, String departmentId, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        applyViewScopeCriteria(criteriaList, studentId, universityId, departmentId);

        if (q != null && !q.isBlank()) {
            criteriaList.add(Criteria.where("title").regex(Pattern.quote(q), "i"));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria).with(pageable);
        List<Achievement> achievements = mongoTemplate.find(query, Achievement.class);
        long total = mongoTemplate.count(new Query(criteria), Achievement.class);

        Page<Achievement> page = new PageImpl<>(achievements, pageable, total);
        return PagedResponse.fromPage(page.map(AchievementResponse::fromAchievement));
    }

    private void enforceViewScope(Achievement achievement) {
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return;
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            if (!myUniversityId.equals(achievement.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            String myDepartmentId = getMyDepartmentIdOrThrow();
            if (!myDepartmentId.equals(achievement.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("STUDENT")) {
            Student student = studentRepository.findByUserId(SecurityUtils.getCurrentUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));
            if (!student.getId().equals(achievement.getStudentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    private void enforceManageScope(Achievement achievement) {
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return;
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = getMyUniversityIdOrThrow();
            if (!myUniversityId.equals(achievement.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            String myDepartmentId = getMyDepartmentIdOrThrow();
            if (!myDepartmentId.equals(achievement.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    private void applyViewScopeCriteria(List<Criteria> criteriaList, String studentId, String universityId, String departmentId) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            if (studentId != null && !studentId.isBlank()) {
                criteriaList.add(Criteria.where("studentId").is(studentId));
            }
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
                criteriaList.add(Criteria.where("departmentId").is(departmentId));
            }
            if (studentId != null && !studentId.isBlank()) {
                criteriaList.add(Criteria.where("studentId").is(studentId));
            }
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            String myDepartmentId = getMyDepartmentIdOrThrow();
            criteriaList.add(Criteria.where("departmentId").is(myDepartmentId));
            if (studentId != null && !studentId.isBlank()) {
                criteriaList.add(Criteria.where("studentId").is(studentId));
            }
            return;
        }

        if (SecurityUtils.hasRole("STUDENT")) {
            Student student = studentRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));
            criteriaList.add(Criteria.where("studentId").is(student.getId()));
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    private String getMyUniversityIdOrThrow() {
        return universityRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                .map(University::getId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
    }

    private String getMyDepartmentIdOrThrow() {
        return departmentRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                .map(Department::getId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
    }
}

