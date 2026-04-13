package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateDepartmentRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentCurriculumRequest;
import com.manus.digitalecosystem.dto.request.UpdateDepartmentRequest;
import com.manus.digitalecosystem.dto.response.DepartmentCurriculumResponse;
import com.manus.digitalecosystem.dto.response.DepartmentResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.CurriculumCourse;
import com.manus.digitalecosystem.model.CurriculumSemester;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.DepartmentCurriculum;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.DepartmentService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UniversityRepository universityRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, UniversityRepository universityRepository) {
        this.departmentRepository = departmentRepository;
        this.universityRepository = universityRepository;
    }

    @Override
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        if (!SecurityUtils.hasRole("SUPER_ADMIN") && !SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            throw new AccessDeniedException("Forbidden");
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String currentUserId = SecurityUtils.getCurrentUserId();
            String myUniversityId = universityRepository.findByAdminUserId(currentUserId)
                    .map(University::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
            if (!myUniversityId.equals(request.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
        }

        Department department = Department.builder()
                .universityId(request.getUniversityId())
                .name(request.getName())
                .description(request.getDescription())
                .imageFileId(request.getImageFileId())
                .adminUserId(request.getAdminUserId())
                .build();

        return DepartmentResponse.fromDepartment(departmentRepository.save(department));
    }

    @Override
    public DepartmentResponse getMyDepartment() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Department department = departmentRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
        return DepartmentResponse.fromDepartment(department);
    }

    @Override
    public DepartmentResponse getDepartmentById(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", id));
        return DepartmentResponse.fromDepartment(department);
    }

    @Override
    public PagedResponse<DepartmentResponse> searchDepartments(String q, String universityId, Pageable pageable) {
        Page<Department> page;
        if (universityId != null && !universityId.isBlank()) {
            if (q == null || q.isBlank()) {
                page = departmentRepository.findByUniversityId(universityId, pageable);
            } else {
                page = departmentRepository.findByUniversityIdAndNameContainingIgnoreCase(universityId, q, pageable);
            }
        } else {
            if (q == null || q.isBlank()) {
                page = departmentRepository.findAll(pageable);
            } else {
                page = departmentRepository.findByNameContainingIgnoreCase(q, pageable);
            }
        }

        return PagedResponse.fromPage(page.map(DepartmentResponse::fromDepartment));
    }

    @Override
    public DepartmentResponse updateDepartment(String id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", id));

        boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
        boolean isUniversityAdmin = SecurityUtils.hasRole("UNIVERSITY_ADMIN");
        boolean isDepartmentAdmin = SecurityUtils.hasRole("DEPARTMENT_ADMIN");

        if (isSuperAdmin) {
            // allowed
        } else if (isDepartmentAdmin) {
            if (!SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
                throw new AccessDeniedException("Forbidden");
            }
        } else if (isUniversityAdmin) {
            String myUniversityId = universityRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                    .map(University::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
            if (!myUniversityId.equals(department.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
        } else {
            throw new AccessDeniedException("Forbidden");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setImageFileId(request.getImageFileId());

        if (isSuperAdmin || isUniversityAdmin) {
            if (request.getAdminUserId() != null && !request.getAdminUserId().isBlank()) {
                department.setAdminUserId(request.getAdminUserId());
            }
        }

        return DepartmentResponse.fromDepartment(departmentRepository.save(department));
    }

    @Override
    public void deleteDepartment(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", id));

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            departmentRepository.deleteById(id);
            return;
        }

        if (!SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            throw new AccessDeniedException("Forbidden");
        }

        String myUniversityId = universityRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                .map(University::getId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
        if (!myUniversityId.equals(department.getUniversityId())) {
            throw new AccessDeniedException("Forbidden");
        }

        departmentRepository.deleteById(id);
    }

    @Override
    public DepartmentCurriculumResponse getCurriculum(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));
        return DepartmentCurriculumResponse.fromCurriculum(department.getCurriculum());
    }

    @Override
    public DepartmentCurriculumResponse updateCurriculum(String departmentId, UpdateDepartmentCurriculumRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", departmentId));

        boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
        boolean isUniversityAdmin = SecurityUtils.hasRole("UNIVERSITY_ADMIN");
        boolean isDepartmentAdmin = SecurityUtils.hasRole("DEPARTMENT_ADMIN");

        if (isSuperAdmin) {
            // allowed
        } else if (isDepartmentAdmin) {
            if (!SecurityUtils.getCurrentUserId().equals(department.getAdminUserId())) {
                throw new AccessDeniedException("Forbidden");
            }
        } else if (isUniversityAdmin) {
            String myUniversityId = universityRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                    .map(University::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
            if (!myUniversityId.equals(department.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
        } else {
            throw new AccessDeniedException("Forbidden");
        }

        DepartmentCurriculum curriculum = DepartmentCurriculum.builder()
                .departmentGoals(request.getDepartmentGoals())
                .finalOutcomes(request.getFinalOutcomes())
                .courses(mapCourses(request.getCourses()))
                .semesters(mapSemesters(request.getSemesters()))
                .build();

        department.setCurriculum(curriculum);
        Department saved = departmentRepository.save(department);
        return DepartmentCurriculumResponse.fromCurriculum(saved.getCurriculum());
    }

    private List<CurriculumCourse> mapCourses(List<UpdateDepartmentCurriculumRequest.Course> courses) {
        if (courses == null) {
            return null;
        }
        return courses.stream()
                .map(course -> CurriculumCourse.builder()
                        .code(course.getCode())
                        .title(course.getTitle())
                        .description(course.getDescription())
                        .outcomes(course.getOutcomes())
                        .skills(course.getSkills())
                        .prerequisites(course.getPrerequisites())
                        .build())
                .toList();
    }

    private List<CurriculumSemester> mapSemesters(List<UpdateDepartmentCurriculumRequest.Semester> semesters) {
        if (semesters == null) {
            return null;
        }
        return semesters.stream()
                .map(semester -> CurriculumSemester.builder()
                        .number(semester.getNumber())
                        .outcomes(semester.getOutcomes())
                        .courseCodes(semester.getCourseCodes())
                        .build())
                .toList();
    }
}
