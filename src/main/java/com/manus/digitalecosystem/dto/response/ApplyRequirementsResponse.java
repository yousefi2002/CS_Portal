package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.enums.OpportunityType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApplyRequirementsResponse {
    private OpportunityType opportunityType;
    private String opportunityId;
    private String companyId;
    private List<String> requirements;
    private boolean directApply;
}
