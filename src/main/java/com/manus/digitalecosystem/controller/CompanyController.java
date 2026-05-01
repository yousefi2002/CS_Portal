package com.manus.digitalecosystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manus.digitalecosystem.dto.request.CreateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.request.UpdateCompanyProfileRequest;
import com.manus.digitalecosystem.dto.response.CompanyResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.service.CompanyService;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/companies"})
public class CompanyController {

    private final CompanyService companyService;
    private final ApiResponseFactory apiResponseFactory;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public CompanyController(CompanyService companyService,
                             ApiResponseFactory apiResponseFactory,
                             ObjectMapper objectMapper,
                             Validator validator) {
        this.companyService = companyService;
        this.apiResponseFactory = apiResponseFactory;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<CompanyResponse>> createCompany(@RequestPart(value = "data", required = false) String data,
                                                                   @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        CreateCompanyProfileRequest request = parseRequest(data, CreateCompanyProfileRequest.class, "error.company.data.required");
        return apiResponseFactory.success(HttpStatus.CREATED, "success.company.created", companyService.createCompany(request, images));
    }

    @PatchMapping("/{companyId}/data")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<CompanyResponse>> updateCompanyData(@PathVariable String companyId,
                                                                       @Valid @RequestBody UpdateCompanyProfileRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.company.updated", companyService.updateCompanyData(companyId, request));
    }

    @PatchMapping(value = "/{companyId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<CompanyResponse>> updateCompanyImages(@PathVariable String companyId,
                                                                         @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return apiResponseFactory.success(HttpStatus.OK, "success.company.updated", companyService.updateCompanyImages(companyId, images));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<CompanyResponse>>> getAllCompanies() {
        return apiResponseFactory.success(HttpStatus.OK, "success.company.list", companyService.getAllCompanies());
    }

    @GetMapping("/{companyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<CompanyResponse>> getCompanyById(@PathVariable String companyId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.company.fetched", companyService.getCompanyById(companyId));
    }

    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteCompany(@PathVariable String companyId) {
        companyService.deleteCompany(companyId);
        return apiResponseFactory.success(HttpStatus.OK, "success.company.deleted", null);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<CompanyResponse>>> searchCompanies(@RequestParam(required = false) String search) {
        return apiResponseFactory.success(HttpStatus.OK, "success.company.search", companyService.searchCompanies(search));
    }

    private <T> T parseRequest(String data, Class<T> requestType, String errorMessageKey) {
        if (data == null || data.isBlank()) {
            throw new BadRequestException(errorMessageKey);
        }

        try {
            T request = objectMapper.readValue(data, requestType);
            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new BadRequestException("error.validation.failed");
            }
            return request;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("error.validation.failed");
        }
    }
}
