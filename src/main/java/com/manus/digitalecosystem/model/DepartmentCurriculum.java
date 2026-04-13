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
public class DepartmentCurriculum {
    private List<String> departmentGoals;
    private List<CurriculumCourse> courses;
    private List<CurriculumSemester> semesters;
    private List<String> finalOutcomes;
}

