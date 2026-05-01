package com.manus.digitalecosystem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignCoursesToSemesterRequest {

    @NotEmpty(message = "{validation.course.ids.required}")
    private List<String> courseIds;
}