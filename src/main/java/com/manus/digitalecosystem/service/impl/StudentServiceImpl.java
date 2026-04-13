package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateStudentProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateStudentProfileRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.StudentResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.VerificationStatus;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.StudentService;
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
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final MongoTemplate mongoTemplate;

    public StudentServiceImpl(
            StudentRepository studentRepository,
            UniversityRepository universityRepository,
            DepartmentRepository departmentRepository,
            MongoTemplate mongoTemplate
    ) {
        this.studentRepository = studentRepository;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public StudentResponse createMyStudent(CreateStudentProfileRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        String currentEmail = SecurityUtils.getCurrentUserDetails().getEmail();

        if (studentRepository.findByUserId(currentUserId).isPresent()) {
            throw new DuplicateResourceException("error.student.profile.exists");
        }

        if (!universityRepository.existsById(request.getUniversityId())) {
            throw new ResourceNotFoundException("error.university.not_found", request.getUniversityId());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("error.department.not_found", request.getDepartmentId()));

        if (!request.getUniversityId().equals(department.getUniversityId())) {
            throw new BadRequestException("error.department.university_mismatch");
        }

        Student student = Student.builder()
                .userId(currentUserId)
                .email(currentEmail)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .bio(request.getBio())
                .skills(request.getSkills())
                .imageFileId(request.getImageFileId())
                .universityId(request.getUniversityId())
                .departmentId(request.getDepartmentId())
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        return StudentResponse.fromStudent(studentRepository.save(student));
    }

    @Override
    public StudentResponse getMyStudent() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));
        return StudentResponse.fromStudent(student);
    }

    @Override
    public StudentResponse updateMyStudent(UpdateStudentProfileRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));

        student.setFullName(request.getFullName());
        student.setPhone(request.getPhone());
        student.setBio(request.getBio());
        student.setSkills(request.getSkills());
        student.setImageFileId(request.getImageFileId());

        return StudentResponse.fromStudent(studentRepository.save(student));
    }

    @Override
    public PagedResponse<StudentResponse> searchStudents(
            String q,
            String skill,
            String universityId,
            String departmentId,
            VerificationStatus status,
            Pageable pageable
    ) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
        boolean isUniversityAdmin = SecurityUtils.hasRole("UNIVERSITY_ADMIN");
        boolean isDepartmentAdmin = SecurityUtils.hasRole("DEPARTMENT_ADMIN");

        if (!isSuperAdmin && status != null && status != VerificationStatus.APPROVED) {
            if (isUniversityAdmin) {
                String myUniversityId = universityRepository.findByAdminUserId(currentUserId)
                        .map(University::getId)
                        .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
                if (universityId == null || !myUniversityId.equals(universityId)) {
                    throw new AccessDeniedException("Forbidden");
                }
            } else if (isDepartmentAdmin) {
                String myDepartmentId = departmentRepository.findByAdminUserId(currentUserId)
                        .map(Department::getId)
                        .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
                if (departmentId == null || !myDepartmentId.equals(departmentId)) {
                    throw new AccessDeniedException("Forbidden");
                }
            } else {
                throw new AccessDeniedException("Forbidden");
            }
        }

        VerificationStatus effectiveStatus = status;
        if (!isSuperAdmin && effectiveStatus == null) {
            effectiveStatus = VerificationStatus.APPROVED;
        }

        List<Criteria> criteriaList = new ArrayList<>();
        if (effectiveStatus != null) {
            criteriaList.add(Criteria.where("verificationStatus").is(effectiveStatus));
        }
        if (universityId != null && !universityId.isBlank()) {
            criteriaList.add(Criteria.where("universityId").is(universityId));
        }
        if (departmentId != null && !departmentId.isBlank()) {
            criteriaList.add(Criteria.where("departmentId").is(departmentId));
        }

        if (q != null && !q.isBlank()) {
            criteriaList.add(Criteria.where("fullName").regex(Pattern.quote(q), "i"));
        }
        if (skill != null && !skill.isBlank()) {
            criteriaList.add(Criteria.where("skills").regex(Pattern.quote(skill), "i"));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria).with(pageable);
        List<Student> students = mongoTemplate.find(query, Student.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Student.class);
        Page<Student> page = new PageImpl<>(students, pageable, total);

        return PagedResponse.fromPage(page.map(StudentResponse::fromStudent));
    }

    @Override
    public StudentResponse getStudentById(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", id));

        if (student.getVerificationStatus() == VerificationStatus.APPROVED) {
            return StudentResponse.fromStudent(student);
        }

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.hasRole("SUPER_ADMIN") || currentUserId.equals(student.getUserId())) {
            return StudentResponse.fromStudent(student);
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = universityRepository.findByAdminUserId(currentUserId)
                    .map(University::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
            if (myUniversityId.equals(student.getUniversityId())) {
                return StudentResponse.fromStudent(student);
            }
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            String myDepartmentId = departmentRepository.findByAdminUserId(currentUserId)
                    .map(Department::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
            if (myDepartmentId.equals(student.getDepartmentId())) {
                return StudentResponse.fromStudent(student);
            }
        }

        throw new AccessDeniedException("Forbidden");
    }

    @Override
    public StudentResponse updateVerificationStatus(String id, VerificationStatus verificationStatus) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.not_found", id));

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            student.setVerificationStatus(verificationStatus);
            return StudentResponse.fromStudent(studentRepository.save(student));
        }

        if (!SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            throw new AccessDeniedException("Forbidden");
        }

        String myUniversityId = universityRepository.findByAdminUserId(SecurityUtils.getCurrentUserId())
                .map(University::getId)
                .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
        if (!myUniversityId.equals(student.getUniversityId())) {
            throw new AccessDeniedException("Forbidden");
        }

        student.setVerificationStatus(verificationStatus);
        return StudentResponse.fromStudent(studentRepository.save(student));
    }
}
