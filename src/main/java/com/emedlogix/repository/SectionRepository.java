package com.emedlogix.repository;

import com.emedlogix.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends JpaRepository<Section, String> {
    Section findFirstByCodeAndVersion(String code,String version);
}
