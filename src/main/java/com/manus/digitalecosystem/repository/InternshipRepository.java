package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Internship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends MongoRepository<Internship, String> {
    Page<Internship> findByCompanyId(String companyId, Pageable pageable);

    Page<Internship> findByCompanyIdAndDeletedFalse(String companyId, Pageable pageable);

    Page<Internship> findByInternshipTitleContainingIgnoreCase(String internshipTitle, Pageable pageable);

    Page<Internship> findByInternshipTitleContainingIgnoreCaseAndDeletedFalse(String internshipTitle, Pageable pageable);

    Page<Internship> findByCompanyIdAndInternshipTitleContainingIgnoreCase(String companyId, String internshipTitle, Pageable pageable);

    Page<Internship> findByCompanyIdAndInternshipTitleContainingIgnoreCaseAndDeletedFalse(String companyId, String internshipTitle, Pageable pageable);

    List<Internship> findByDeletedFalse();
}

