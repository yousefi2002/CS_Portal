package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findByAdminUserId(String adminUserId);

    Page<Company> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);

    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Company> findByVerificationStatusAndNameContainingIgnoreCase(
            VerificationStatus verificationStatus,
            String name,
            Pageable pageable
    );
}

