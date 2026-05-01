package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCourseRequest {

    private String code;

    private Integer credits;

    private LocalizedText title;

    private LocalizedText description;

    private List<LocalizedText> outcomes;

    private List<LocalizedText> skills;

    private List<LocalizedText> prerequisites;
}