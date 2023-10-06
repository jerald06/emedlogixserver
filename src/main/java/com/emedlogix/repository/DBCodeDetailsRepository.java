package com.emedlogix.repository;

import com.emedlogix.entity.CodeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DBCodeDetailsRepository extends JpaRepository<CodeDetails, String> {
   // CodeDetails findByCode(String code,String version);

    CodeDetails findFirstByCodeAndVersion(String code, String version);
}
