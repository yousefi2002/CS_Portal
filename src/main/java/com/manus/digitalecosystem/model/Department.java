package com.manus.digitalecosystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "departments")
public class Department {

    @Id
    private String id;

    @Indexed
    private String universityId;

    private LocalizedText name;

    private LocalizedText description;

    @Builder.Default
    private List<String> imageFileIds = new ArrayList<>();

    @Indexed
    private String adminUserId;

    private List<LocalizedText> goals;

    private List<LocalizedText> outcomes;

    private List<CurriculumSemester> semesters;

    @Builder.Default
    private boolean deleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

