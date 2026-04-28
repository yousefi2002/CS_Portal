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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "universities")
public class University {

    @Id
    private String id;

    private LocalizedText name;

    private LocalizedText description;

    private LocalizedText address;

    private String website;

    private String phone;

    private String email;

    private String imageFileId;

    @Indexed
    private String adminUserId;

    @Indexed
    private VerificationStatus verificationStatus;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

