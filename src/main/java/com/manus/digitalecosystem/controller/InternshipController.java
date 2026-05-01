package com.manus.digitalecosystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manus.digitalecosystem.dto.request.CreateInternshipRequest;
import com.manus.digitalecosystem.dto.request.DeleteInternshipRequest;
import com.manus.digitalecosystem.dto.request.UpdateInternshipRequest;
import com.manus.digitalecosystem.dto.response.InternshipResponse;
import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.exception.BadRequestException;
import com.manus.digitalecosystem.service.InternshipService;
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
@RequestMapping({"/api/v1/internships"})
public class InternshipController {

    private final InternshipService internshipService;
    private final ApiResponseFactory apiResponseFactory;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public InternshipController(InternshipService internshipService,
                                ApiResponseFactory apiResponseFactory,
                                ObjectMapper objectMapper,
                                Validator validator) {
        this.internshipService = internshipService;
        this.apiResponseFactory = apiResponseFactory;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<InternshipResponse>> createInternship(@RequestPart(value = "data", required = false) String data,
                                                                         @RequestPart(value = "image", required = false) MultipartFile image) {
        CreateInternshipRequest request = parseRequest(data, CreateInternshipRequest.class, "error.internship.data.required");
        return apiResponseFactory.success(HttpStatus.CREATED, "success.internship.created", internshipService.createInternship(request, image));
    }

    @PatchMapping("/{internshipId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<InternshipResponse>> updateInternshipData(@PathVariable String internshipId,
                                                                             @Valid @RequestBody UpdateInternshipRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.internship.updated",
                internshipService.updateInternshipData(internshipId, request));
    }

    @PatchMapping(value = "/{internshipId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<InternshipResponse>> updateInternshipImage(@PathVariable String internshipId,
                                                                              @RequestPart(value = "image", required = false) MultipartFile image) {
        return apiResponseFactory.success(HttpStatus.OK, "success.internship.updated",
                internshipService.updateInternshipImage(internshipId, image));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<List<InternshipResponse>>> getAllInternships() {
        return apiResponseFactory.success(HttpStatus.OK, "success.internship.list", internshipService.getAllInternships());
    }

    @GetMapping("/{internshipId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<InternshipResponse>> getInternshipById(@PathVariable String internshipId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.internship.fetched", internshipService.getInternshipById(internshipId));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<List<InternshipResponse>>> searchInternships(@RequestParam(required = false) String search,
                                                                                @RequestParam(required = false) String companyId) {
        return apiResponseFactory.success(HttpStatus.OK, "success.internship.search",
                internshipService.searchInternships(search, companyId));
    }

    @PatchMapping("/{internshipId}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Response<InternshipResponse>> softDeleteInternship(@PathVariable String internshipId,
                                                                             @Valid @RequestBody DeleteInternshipRequest request) {
        return apiResponseFactory.success(HttpStatus.OK, "success.internship.deleted",
                internshipService.softDeleteInternship(internshipId, request));
    }

    @DeleteMapping("/{internshipId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Response<Object>> deleteInternship(@PathVariable String internshipId) {
        internshipService.deleteInternship(internshipId);
        return apiResponseFactory.success(HttpStatus.OK, "success.internship.deleted", null);
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
