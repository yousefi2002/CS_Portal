package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateTaskRequest;
import com.manus.digitalecosystem.dto.request.UpdateTaskCompletionRequest;
import com.manus.digitalecosystem.dto.request.UpdateTaskRequest;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.dto.response.TaskResponse;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse updateTask(String id, UpdateTaskRequest request);

    void deleteTask(String id);

    TaskResponse getTaskById(String id);

    PagedResponse<TaskResponse> searchTasks(String q, String universityId, String departmentId, Boolean assignedToMe, Pageable pageable);

    TaskResponse updateMyCompletion(String id, UpdateTaskCompletionRequest request);
}

