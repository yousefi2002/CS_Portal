package com.manus.digitalecosystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementContributor {

    private String studentId;

    private String name;

    private String imageFileId;

    private String role;

    private boolean isExternal;
}
