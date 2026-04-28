package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class UpdateDepartmentCurriculumRequest {
    private List<LocalizedText> departmentGoals;
    private List<Course> courses;
    private List<Semester> semesters;
    private List<LocalizedText> finalOutcomes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Course {
        private String code;
        private LocalizedText title;
        private LocalizedText description;
        private List<LocalizedText> outcomes;
        private List<LocalizedText> skills;
        private List<LocalizedText> prerequisites;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Semester {
        private int number;
        private List<LocalizedText> outcomes;
        private List<String> courseCodes;
    }
}

