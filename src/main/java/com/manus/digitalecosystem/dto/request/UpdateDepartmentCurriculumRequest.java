package com.manus.digitalecosystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class UpdateDepartmentCurriculumRequest {
    private List<String> departmentGoals;
    private List<Course> courses;
    private List<Semester> semesters;
    private List<String> finalOutcomes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Course {
        private String code;
        private String title;
        private String description;
        private List<String> outcomes;
        private List<String> skills;
        private List<String> prerequisites;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Semester {
        private int number;
        private List<String> outcomes;
        private List<String> courseCodes;
    }
}

