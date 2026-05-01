package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.AchievementContributor;
import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAchievementRequest {

    private LocalizedText title;

    private LocalizedText description;

    private String link;

    private List<AchievementContributor> contributors;

    private String type;
}

