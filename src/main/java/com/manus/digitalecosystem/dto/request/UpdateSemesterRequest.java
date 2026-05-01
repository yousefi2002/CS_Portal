package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSemesterRequest {

    private List<LocalizedText> goals;

    private List<LocalizedText> outcomes;

    private List<String> courseIds;
}