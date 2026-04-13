package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.VerificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    Optional<Student> findByUserId(String userId);

    List<Student> findByUniversityId(String universityId);

    List<Student> findByDepartmentId(String departmentId);

    List<Student> findByVerificationStatus(VerificationStatus verificationStatus);
}
