package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.University;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends MongoRepository<University, String> {
    Optional<University> findByAdminUserId(String adminUserId);
}

