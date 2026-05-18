package com.manus.digitalecosystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomePageResponse {
    private List<UniversityResponse> universities;
    private List<CompanyResponse> companies;
    private com.manus.digitalecosystem.dto.response.Pagination universitiesPagination;
    private com.manus.digitalecosystem.dto.response.Pagination companiesPagination;
}