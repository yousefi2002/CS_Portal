package com.manus.digitalecosystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopStudentResponse {
    private String studentId;
    private String fullName;
    private String email;
    private String imageFileId;
    private long achievementCount;
}
