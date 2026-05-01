package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AchievementRepository extends MongoRepository<Achievement, String> {

	List<Achievement> findByDeletedFalse();

	List<Achievement> findByUniversityIdAndDeletedFalse(String universityId);

	List<Achievement> findByDepartmentIdAndDeletedFalse(String departmentId);

	List<Achievement> findByDepartmentIdInAndDeletedFalse(Collection<String> departmentIds);

	List<Achievement> findByCompanyIdAndDeletedFalse(String companyId);
}

