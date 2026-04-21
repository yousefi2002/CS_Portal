package com.manus.digitalecosystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumSemester {
    @Indexed(unique = true)
    private int number;
    private List<String> outcomes;
    private List<String> goals;
    private List<String> courseIds;
}

