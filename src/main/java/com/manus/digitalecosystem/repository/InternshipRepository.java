package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Internship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternshipRepository extends MongoRepository<Internship, String> {
    Page<Internship> findByCompanyId(String companyId, Pageable pageable);

    Page<Internship> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Internship> findByCompanyIdAndTitleContainingIgnoreCase(String companyId, String title, Pageable pageable);
}

