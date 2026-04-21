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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "students")
public class Student {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String email;

    private String fullName;

    private String phone;

    private String bio;

    private List<String> skills;

    private String imageFileId;

    @Indexed
    private String universityId;

    @Indexed
    private String departmentId;

    @Indexed
    private VerificationStatus verificationStatus;

    private boolean isTopStudent;

    private String topStudentCategory;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

