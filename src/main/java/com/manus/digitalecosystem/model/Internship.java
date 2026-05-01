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

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "internships")
public class Internship {

    @Id
    private String id;

    @Indexed
    private String companyId;

    private LocalizedText companyName;

    private LocalizedText companyDescription;

    private String companyImage;

    private LocalizedText internshipTitle;

    private LocalizedText internshipDescription;

    private LocalizedText requirements;

    private LocalizedText roadmap;

    private LocalizedText duration;

    private int vacancies;

    private List<LocalizedText> requiredSkills;

    private List<String> applicationRequirements;

    @Builder.Default
    private boolean deleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

