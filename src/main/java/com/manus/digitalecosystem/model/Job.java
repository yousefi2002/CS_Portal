package com.manus.digitalecosystem.model;

import com.manus.digitalecosystem.model.enums.JobType;
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
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    @Indexed
    private String companyId;

    private LocalizedText companyName;

    private LocalizedText companyDescription;

    private String companyImage;

    private LocalizedText jobTitle;

    private LocalizedText jobDescription;

    private double salary;

    private JobType type;

    private int vacancies;

    private List<LocalizedText> requirement;

    private LocalizedText location;

    private List<LocalizedText> requiredSkills;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

