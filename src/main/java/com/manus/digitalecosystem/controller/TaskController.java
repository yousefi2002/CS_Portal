package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.request.CreateTaskRequest;
import com.manus.digitalecosystem.dto.request.UpdateTaskCompletionRequest;
import com.manus.digitalecosystem.dto.request.UpdateTaskRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.TaskResponse;
import com.manus.digitalecosystem.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Department tasks (CRUD + search + pagination)")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DEPARTMENT_ADMIN')")
    @Operation(summary = "Create a task (Department Admin)")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Update a task (Department Admin / University Admin / Super Admin)")
    public ResponseEntity<TaskResponse> update(@PathVariable String id, @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN')")
    @Operation(summary = "Delete a task (Department Admin / University Admin / Super Admin)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN','STUDENT')")
    @Operation(summary = "Get task by id")
    public ResponseEntity<TaskResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','DEPARTMENT_ADMIN','STUDENT')")
    @Operation(summary = "Search tasks")
    public ResponseEntity<PagedResponse<TaskResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) Boolean assignedToMe,
            Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.searchTasks(q, universityId, departmentId, assignedToMe, pageable));
    }

    @PatchMapping("/{id}/completion")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update my completion status (Student)")
    public ResponseEntity<TaskResponse> updateCompletion(@PathVariable String id, @Valid @RequestBody UpdateTaskCompletionRequest request) {
        return ResponseEntity.ok(taskService.updateMyCompletion(id, request));
    }
}

