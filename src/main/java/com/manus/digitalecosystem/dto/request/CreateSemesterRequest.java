package com.manus.digitalecosystem.dto.request;

import com.manus.digitalecosystem.model.LocalizedText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateSemesterRequest {

    @NotNull(message = "{validation.semester.number.required}")
    private Integer number;

    private List<LocalizedText> goals;

    private List<LocalizedText> outcomes;

    private List<String> courseIds;
}