package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends MongoRepository<Achievement, String> {
}

