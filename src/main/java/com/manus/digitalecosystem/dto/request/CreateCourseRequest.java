package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateCourseRequest {

    @NotNull(message = "{validation.course.department_id.required}")
    private String departmentId;

    @NotNull(message = "{validation.course.code.required}")
    private String code;

    @NotNull(message = "{validation.course.credits.required}")
    private Integer credits;

    @NotNull(message = "{validation.course.title.required}")
    private LocalizedText title;

    private LocalizedText description;

    private List<LocalizedText> outcomes;

    private List<LocalizedText> skills;

    private List<LocalizedText> prerequisites;
}