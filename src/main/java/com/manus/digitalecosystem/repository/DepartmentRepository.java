package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findByAdminUserId(String adminUserId);

    Page<Department> findByUniversityId(String universityId, Pageable pageable);

    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Department> findByUniversityIdAndNameContainingIgnoreCase(String universityId, String name, Pageable pageable);
}

