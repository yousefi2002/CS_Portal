package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Application;
import com.manus.digitalecosystem.model.enums.OpportunityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    boolean existsByStudentIdAndOpportunityTypeAndOpportunityId(String studentId, OpportunityType opportunityType, String opportunityId);

    Page<Application> findByStudentId(String studentId, Pageable pageable);

    Page<Application> findByCompanyId(String companyId, Pageable pageable);

    Page<Application> findByCompanyIdAndOpportunityType(String companyId, OpportunityType opportunityType, Pageable pageable);

    Page<Application> findByCompanyIdAndOpportunityTypeAndOpportunityId(
            String companyId,
            OpportunityType opportunityType,
            String opportunityId,
            Pageable pageable
    );
}

