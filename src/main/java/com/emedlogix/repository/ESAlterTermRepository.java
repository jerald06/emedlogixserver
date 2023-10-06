package com.emedlogix.repository;

import com.emedlogix.entity.AlterTerm;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ESAlterTermRepository extends ElasticsearchRepository<AlterTerm, String> {
    @Query("{\"bool\": {\"must\": [{\"match\": {\"alterDescription\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}], \"filter\": [{\"term\": {\"version\": \"?1\"}}]}}")
    List<AlterTerm> findByAlterDescriptionAndVersion(String query,String version);

    Optional<AlterTerm> findByCode(String code);
}