package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private String universityId;
    private String departmentId;
    private String title;
    private String description;
    private Instant dueAt;
    private boolean assignedToAll;
    private int assignedCount;
    private int completedCount;
    private boolean completedByMe;
    private String createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse fromTask(Task task, String currentUserId) {
        int assignedCount = task.getAssignedToUserIds() == null ? 0 : task.getAssignedToUserIds().size();
        int completedCount = task.getCompletedByUserIds() == null ? 0 : task.getCompletedByUserIds().size();
        boolean completedByMe = task.getCompletedByUserIds() != null && currentUserId != null
                && task.getCompletedByUserIds().contains(currentUserId);

        return TaskResponse.builder()
                .id(task.getId())
                .universityId(task.getUniversityId())
                .departmentId(task.getDepartmentId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueAt(task.getDueAt())
                .assignedToAll(task.isAssignedToAll())
                .assignedCount(assignedCount)
                .completedCount(completedCount)
                .completedByMe(completedByMe)
                .createdByUserId(task.getCreatedByUserId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
