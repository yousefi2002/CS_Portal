package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.response.CompanyResponse;
import com.manus.digitalecosystem.dto.response.HomePageResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.dto.response.UniversityResponse;
import com.manus.digitalecosystem.service.CompanyService;
import com.manus.digitalecosystem.service.UniversityService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/home"})
public class HomePageController {

    private final UniversityService universityService;
    private final CompanyService companyService;
    private final ApiResponseFactory apiResponseFactory;

    public HomePageController(UniversityService universityService,
                              CompanyService companyService,
                              ApiResponseFactory apiResponseFactory) {
        this.universityService = universityService;
        this.companyService = companyService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Response<HomePageResponse>> getHomePageData(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page) {
        final int size = 10; // fixed 10 universities and 10 companies per page as requested

        org.springframework.data.domain.Page<UniversityResponse> universitiesPage = universityService.getAllUniversities(page, size);
        org.springframework.data.domain.Page<com.manus.digitalecosystem.dto.response.CompanyResponse> companiesPage = companyService.getAllCompaniesForHome(page, size);

        com.manus.digitalecosystem.dto.response.Pagination uniPagination = com.manus.digitalecosystem.dto.response.Pagination.builder()
            .page(universitiesPage.getNumber())
            .size(universitiesPage.getSize())
            .totalElements(universitiesPage.getTotalElements())
            .totalPages(universitiesPage.getTotalPages())
            .build();

        com.manus.digitalecosystem.dto.response.Pagination compPagination = com.manus.digitalecosystem.dto.response.Pagination.builder()
            .page(companiesPage.getNumber())
            .size(companiesPage.getSize())
            .totalElements(companiesPage.getTotalElements())
            .totalPages(companiesPage.getTotalPages())
            .build();

        HomePageResponse response = HomePageResponse.builder()
            .universities(universitiesPage.getContent())
            .companies(companiesPage.getContent())
            .universitiesPagination(uniPagination)
            .companiesPagination(compPagination)
            .build();

        return apiResponseFactory.success(HttpStatus.OK, "success.home_page.fetched", response);
        }
}