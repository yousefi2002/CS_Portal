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

@Document(collection = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(def = "{'departmentId': 1, 'code': 1}", unique = true)
public class Course {

    @Id
    private String id;

    @Indexed
    private String departmentId;

    private String code;
    private int credits;

    private String title;
    private String description;
    private String roadMapImage;

    private List<String> outcomes;
    private List<String> skills;
    private List<String> prerequisites;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

