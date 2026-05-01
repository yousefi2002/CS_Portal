package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateApplicationRequest;
import com.manus.digitalecosystem.dto.request.DeleteApplicationRequest;
import com.manus.digitalecosystem.dto.request.UpdateApplicationRequest;
import com.manus.digitalecosystem.dto.response.ApplyRequirementsResponse;
import com.manus.digitalecosystem.dto.response.ApplicationResponse;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse createApplication(CreateApplicationRequest request);

    ApplicationResponse updateApplication(String applicationId, UpdateApplicationRequest request);

    List<ApplicationResponse> getAllApplications();

    ApplicationResponse getApplicationById(String applicationId);

    List<ApplicationResponse> getApplicationsByCompany(String companyId);

    List<ApplicationResponse> getApplicationsByStudent(String studentId);

    ApplicationResponse softDeleteApplication(String applicationId, DeleteApplicationRequest request);

    void deleteApplication(String applicationId);

    List<ApplicationResponse> searchApplicationsByCompany(String companyId, String query);

    List<ApplicationResponse> searchApplicationsGlobal(String query);

    ApplyRequirementsResponse getApplyRequirements(String opportunityType, String opportunityId);
}
