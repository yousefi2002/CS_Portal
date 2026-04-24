package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    Page<Job> findByCompanyId(String companyId, Pageable pageable);

    Page<Job> findByJobTitleContainingIgnoreCase(String jobTitle, Pageable pageable);

    Page<Job> findByCompanyIdAndJobTitleContainingIgnoreCase(String companyId, String jobTitle, Pageable pageable);
}

