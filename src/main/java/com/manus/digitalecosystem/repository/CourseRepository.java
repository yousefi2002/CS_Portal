package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByDepartmentId(String departmentId);

    List<Course> findByDepartmentIdIn(Collection<String> departmentIds);
}