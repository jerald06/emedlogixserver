package com.emedlogix.repository;

import com.emedlogix.entity.AlterTerm;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ESAlterTermRepository extends ElasticsearchRepository<AlterTerm, String> {
    @Query("{\"query_string\" : {\"query\" : \"?0~\"}}")
    List<AlterTerm> findByAlterDescription(String query);

    Optional<AlterTerm> findByCode(String code);
}