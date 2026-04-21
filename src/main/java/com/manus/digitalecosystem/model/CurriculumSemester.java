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
    private List<String> outcomes;
    private List<String> goals;
    private List<String> courseIds;
}
