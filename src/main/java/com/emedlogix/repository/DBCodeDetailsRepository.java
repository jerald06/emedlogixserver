package com.emedlogix.repository;

import com.emedlogix.entity.CodeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DBCodeDetailsRepository extends JpaRepository<CodeDetails, String> {
    CodeDetails findByCode(String code);
}
