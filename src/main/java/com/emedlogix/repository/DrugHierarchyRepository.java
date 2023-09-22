package com.emedlogix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.emedlogix.entity.DrugHierarchy;

@Repository
public interface DrugHierarchyRepository extends JpaRepository<DrugHierarchy, Integer> {

}
