package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateInternshipRequest;
import com.manus.digitalecosystem.dto.request.DeleteInternshipRequest;
import com.manus.digitalecosystem.dto.request.UpdateInternshipRequest;
import com.manus.digitalecosystem.dto.response.InternshipResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InternshipService {

    InternshipResponse createInternship(CreateInternshipRequest request, MultipartFile image);

    InternshipResponse updateInternshipData(String internshipId, UpdateInternshipRequest request);

    InternshipResponse updateInternshipImage(String internshipId, MultipartFile image);

    List<InternshipResponse> getAllInternships();

    InternshipResponse getInternshipById(String internshipId);

    List<InternshipResponse> searchInternships(String search, String companyId);

    InternshipResponse softDeleteInternship(String internshipId, DeleteInternshipRequest request);

    void deleteInternship(String internshipId);
}
