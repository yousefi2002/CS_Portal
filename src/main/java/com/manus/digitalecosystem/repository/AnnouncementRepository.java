package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Announcement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends MongoRepository<Announcement, String> {
	List<Announcement> findByDeletedFalse(Pageable pageable);

	List<Announcement> findByUniversityIdAndDepartmentIdIsNull(String universityId, Pageable pageable);

	List<Announcement> findByUniversityIdAndDepartmentIdIsNullAndDeletedFalse(String universityId, Pageable pageable);

	List<Announcement> findByDepartmentId(String departmentId, Pageable pageable);

	List<Announcement> findByDepartmentIdAndDeletedFalse(String departmentId, Pageable pageable);
}

