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

    private String companyName;

    private String companyDescription;

    private String companyImage;

    private String jobTitle;

    private String jobDescription;

    private double salary;

    private JobType type;

    private int vacancies;

    private List<String> requirement;

    private String location;

    private List<String> requiredSkills;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

