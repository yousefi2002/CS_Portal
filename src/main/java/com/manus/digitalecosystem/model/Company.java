package com.manus.digitalecosystem.model;

import com.manus.digitalecosystem.model.enums.VerificationStatus;
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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "companies")
public class Company {

    @Id
    private String id;

    private LocalizedText name;

    private LocalizedText description;

    private int numberOfEmployees;

    private LocalizedText developmentType;

    private String website;

    private String phone;

    private String email;

    @Builder.Default
    private List<String> imageFileIds = new ArrayList<>();

    @Indexed
    private String adminUserId;

    @Indexed
    private VerificationStatus verificationStatus;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

