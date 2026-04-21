package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends MongoRepository<University, String> {
    Optional<University> findByAdminUserId(String adminUserId);

    Page<University> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);

    Page<University> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<University> findByVerificationStatusAndNameContainingIgnoreCase(
            VerificationStatus verificationStatus,
            String name,
            Pageable pageable
    );
}

