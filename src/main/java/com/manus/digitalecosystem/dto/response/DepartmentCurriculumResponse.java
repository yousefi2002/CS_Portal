package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.DepartmentCurriculum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCurriculumResponse {
    private List<String> departmentGoals;
    private List<Course> courses;
    private List<Semester> semesters;
    private List<String> finalOutcomes;

    public static DepartmentCurriculumResponse fromCurriculum(DepartmentCurriculum curriculum) {
        if (curriculum == null) {
            return DepartmentCurriculumResponse.builder().build();
        }
        return DepartmentCurriculumResponse.builder()
                .departmentGoals(curriculum.getDepartmentGoals())
                .courses(curriculum.getCourses() == null ? null : curriculum.getCourses().stream().map(Course::from).toList())
                .semesters(curriculum.getSemesters() == null ? null : curriculum.getSemesters().stream().map(Semester::from).toList())
                .finalOutcomes(curriculum.getFinalOutcomes())
                .build();
    }

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

        public static Course from(com.manus.digitalecosystem.model.CurriculumCourse course) {
            return new Course(
                    course.getCode(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getOutcomes(),
                    course.getSkills(),
                    course.getPrerequisites()
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Semester {
        private int number;
        private List<String> outcomes;
        private List<String> courseCodes;

        public static Semester from(com.manus.digitalecosystem.model.CurriculumSemester semester) {
            return new Semester(
                    semester.getNumber(),
                    semester.getOutcomes(),
                    semester.getCourseCodes()
            );
        }
    }
}

