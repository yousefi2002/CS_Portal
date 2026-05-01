package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CourseResponse {
    private String id;
    private String departmentId;
    private String code;
    private int credits;
    private LocalizedText title;
    private LocalizedText description;
    private String roadMapImage;
    private List<LocalizedText> outcomes;
    private List<LocalizedText> skills;
    private List<LocalizedText> prerequisites;
    private Instant createdAt;
    private Instant updatedAt;
}