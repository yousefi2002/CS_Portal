package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class StudentResponse {
    private String id;
    private String userId;
    private String email;
    private String fullName;
    private String phone;
    private LocalizedText bio;
    private List<String> skills;
    private String imageFileId;
    private String universityId;
    private String departmentId;
    private VerificationStatus verificationStatus;
    private boolean deleted;
    private boolean graduated;
    private Instant graduatedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
