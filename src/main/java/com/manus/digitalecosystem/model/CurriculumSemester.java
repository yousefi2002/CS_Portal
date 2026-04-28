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
public class CurriculumSemester {
    private int number;
    private List<LocalizedText> outcomes;
    private List<LocalizedText> goals;
    private List<String> courseIds;
}
