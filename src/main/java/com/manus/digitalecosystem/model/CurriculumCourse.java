package com.manus.digitalecosystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumCourse {
    private String code;
    private String title;
    private String description;
    private List<String> outcomes;
    private List<String> skills;
    private List<String> prerequisites;
}

