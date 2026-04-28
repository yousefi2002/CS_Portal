package com.manus.digitalecosystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(def = "{'departmentId': 1, 'code': 1}", unique = true)
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    @Indexed
    private String departmentId;

    private String code;
    private int credits;

    private LocalizedText title;
    private LocalizedText description;
    private String roadMapImage;

    private List<LocalizedText> outcomes;
    private List<LocalizedText> skills;
    private List<LocalizedText> prerequisites;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

