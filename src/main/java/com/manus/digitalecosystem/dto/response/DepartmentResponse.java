package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.CurriculumSemester;
import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DepartmentResponse {
    private String id;
    private String universityId;
    private LocalizedText name;
    private LocalizedText description;
    private List<String> imageFileIds;
    private String adminUserId;
    private List<LocalizedText> goals;
    private List<LocalizedText> outcomes;
    private List<CurriculumSemester> semesters;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}