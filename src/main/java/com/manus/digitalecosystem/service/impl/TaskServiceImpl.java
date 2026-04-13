package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.request.CreateTaskRequest;
import com.manus.digitalecosystem.dto.request.UpdateTaskCompletionRequest;
import com.manus.digitalecosystem.dto.request.UpdateTaskRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.TaskResponse;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.NotificationType;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.Task;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.TaskRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.service.NotificationService;
import com.manus.digitalecosystem.service.TaskService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final DepartmentRepository departmentRepository;
    private final UniversityRepository universityRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final MongoTemplate mongoTemplate;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            DepartmentRepository departmentRepository,
            UniversityRepository universityRepository,
            StudentRepository studentRepository,
            NotificationService notificationService,
            MongoTemplate mongoTemplate
    ) {
        this.taskRepository = taskRepository;
        this.departmentRepository = departmentRepository;
        this.universityRepository = universityRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        if (!SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            throw new AccessDeniedException("Forbidden");
        }

        String currentUserId = SecurityUtils.getCurrentUserId();
        Department myDepartment = departmentRepository.findByAdminUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));

        boolean assignedToAll = request.getAssignedToAll() == null || request.getAssignedToAll();
        List<String> assignedToUserIds = null;
        if (!assignedToAll) {
            if (request.getAssignedToUserIds() == null || request.getAssignedToUserIds().isEmpty()) {
                throw new BadRequestException("error.task.assignees.required");
            }
            assignedToUserIds = uniqueNonBlank(request.getAssignedToUserIds());
        }

        Task task = Task.builder()
                .universityId(myDepartment.getUniversityId())
                .departmentId(myDepartment.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .dueAt(request.getDueAt())
                .assignedToAll(assignedToAll)
                .assignedToUserIds(assignedToUserIds)
                .completedByUserIds(new ArrayList<>())
                .createdByUserId(currentUserId)
                .build();

        Task saved = taskRepository.save(task);

        notifyTaskAssigned(saved, currentUserId);

        return TaskResponse.fromTask(saved, currentUserId);
    }

    @Override
    public TaskResponse updateTask(String id, UpdateTaskRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.task.not_found", id));

        enforceTaskManageScope(task);

        boolean assignedToAll = request.getAssignedToAll() == null || request.getAssignedToAll();
        List<String> assignedToUserIds = null;
        if (!assignedToAll) {
            if (request.getAssignedToUserIds() == null || request.getAssignedToUserIds().isEmpty()) {
                throw new BadRequestException("error.task.assignees.required");
            }
            assignedToUserIds = uniqueNonBlank(request.getAssignedToUserIds());
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueAt(request.getDueAt());
        task.setAssignedToAll(assignedToAll);
        task.setAssignedToUserIds(assignedToUserIds);

        Task saved = taskRepository.save(task);
        return TaskResponse.fromTask(saved, currentUserId);
    }

    @Override
    public void deleteTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.task.not_found", id));

        enforceTaskManageScope(task);
        taskRepository.deleteById(id);
    }

    @Override
    public TaskResponse getTaskById(String id) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.task.not_found", id));

        enforceTaskViewScope(task);
        return TaskResponse.fromTask(task, currentUserId);
    }

    @Override
    public PagedResponse<TaskResponse> searchTasks(String q, String universityId, String departmentId, Boolean assignedToMe, Pageable pageable) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        List<Criteria> criteriaList = new ArrayList<>();

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            if (universityId != null && !universityId.isBlank()) {
                criteriaList.add(Criteria.where("universityId").is(universityId));
            }
            if (departmentId != null && !departmentId.isBlank()) {
                criteriaList.add(Criteria.where("departmentId").is(departmentId));
            }
        } else if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = universityRepository.findByAdminUserId(currentUserId)
                    .map(University::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
            criteriaList.add(Criteria.where("universityId").is(myUniversityId));
            if (departmentId != null && !departmentId.isBlank()) {
                criteriaList.add(Criteria.where("departmentId").is(departmentId));
            }
        } else if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = departmentRepository.findByAdminUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
            criteriaList.add(Criteria.where("departmentId").is(myDepartment.getId()));
        } else if (SecurityUtils.hasRole("STUDENT")) {
            Student student = studentRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));
            criteriaList.add(Criteria.where("departmentId").is(student.getDepartmentId()));
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("assignedToAll").is(true),
                    Criteria.where("assignedToUserIds").in(currentUserId)
            ));
        } else {
            throw new AccessDeniedException("Forbidden");
        }

        if (q != null && !q.isBlank()) {
            criteriaList.add(Criteria.where("title").regex(Pattern.quote(q), "i"));
        }

        if (assignedToMe != null && assignedToMe) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("assignedToAll").is(true),
                    Criteria.where("assignedToUserIds").in(currentUserId)
            ));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria).with(pageable);
        List<Task> tasks = mongoTemplate.find(query, Task.class);
        long total = mongoTemplate.count(new Query(criteria), Task.class);
        Page<Task> page = new PageImpl<>(tasks, pageable, total);

        return PagedResponse.fromPage(page.map(task -> TaskResponse.fromTask(task, currentUserId)));
    }

    @Override
    public TaskResponse updateMyCompletion(String id, UpdateTaskCompletionRequest request) {
        if (!SecurityUtils.hasRole("STUDENT")) {
            throw new AccessDeniedException("Forbidden");
        }

        String currentUserId = SecurityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.task.not_found", id));

        if (!student.getDepartmentId().equals(task.getDepartmentId())) {
            throw new AccessDeniedException("Forbidden");
        }

        boolean assigned = task.isAssignedToAll()
                || (task.getAssignedToUserIds() != null && task.getAssignedToUserIds().contains(currentUserId));
        if (!assigned) {
            throw new AccessDeniedException("Forbidden");
        }

        List<String> completedBy = task.getCompletedByUserIds() == null ? new ArrayList<>() : new ArrayList<>(task.getCompletedByUserIds());
        if (Boolean.TRUE.equals(request.getCompleted())) {
            if (!completedBy.contains(currentUserId)) {
                completedBy.add(currentUserId);
            }
        } else {
            completedBy.remove(currentUserId);
        }
        task.setCompletedByUserIds(completedBy);

        Task saved = taskRepository.save(task);
        return TaskResponse.fromTask(saved, currentUserId);
    }

    private void enforceTaskViewScope(Task task) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return;
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = universityRepository.findByAdminUserId(currentUserId)
                    .map(University::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
            if (!myUniversityId.equals(task.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = departmentRepository.findByAdminUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
            if (!myDepartment.getId().equals(task.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("STUDENT")) {
            Student student = studentRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.student.profile.not_found"));
            if (!student.getDepartmentId().equals(task.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            boolean assigned = task.isAssignedToAll()
                    || (task.getAssignedToUserIds() != null && task.getAssignedToUserIds().contains(currentUserId));
            if (!assigned) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    private void enforceTaskManageScope(Task task) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return;
        }

        if (SecurityUtils.hasRole("UNIVERSITY_ADMIN")) {
            String myUniversityId = universityRepository.findByAdminUserId(currentUserId)
                    .map(University::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.university.profile.not_found"));
            if (!myUniversityId.equals(task.getUniversityId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        if (SecurityUtils.hasRole("DEPARTMENT_ADMIN")) {
            Department myDepartment = departmentRepository.findByAdminUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.department.profile.not_found"));
            if (!myDepartment.getId().equals(task.getDepartmentId())) {
                throw new AccessDeniedException("Forbidden");
            }
            return;
        }

        throw new AccessDeniedException("Forbidden");
    }

    private void notifyTaskAssigned(Task task, String actorUserId) {
        List<String> recipients;
        if (task.isAssignedToAll()) {
            recipients = studentRepository.findByDepartmentId(task.getDepartmentId()).stream()
                    .map(Student::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        } else {
            recipients = task.getAssignedToUserIds() == null ? List.of() : task.getAssignedToUserIds();
        }

        for (String recipient : recipients) {
            if (recipient == null || recipient.isBlank() || recipient.equals(actorUserId)) {
                continue;
            }
            notificationService.createNotification(
                    recipient,
                    NotificationType.DEPARTMENT_TASK,
                    "notification.task.assigned.title",
                    "notification.task.assigned.body",
                    List.of(task.getTitle())
            );
        }
    }

    private List<String> uniqueNonBlank(List<String> values) {
        if (values == null) {
            return List.of();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    set.add(trimmed);
                }
            }
        }
        return new ArrayList<>(set);
    }
}

