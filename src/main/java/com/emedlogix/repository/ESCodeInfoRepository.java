package com.emedlogix.repository;


import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.CodeInfo;

@Repository
public interface ESCodeInfoRepository extends ElasticsearchRepository<CodeInfo, String> {

    CodeInfo getByCode(String code);

    //  List<CodeInfo> findByCodeStartingWith(String code,String version);
    List<CodeInfo> findByCodeStartingWithAndVersion(String code, String version);

    @Query("{\"query_string\": {\"default_field\": \"description\", \"query\": \"*?0*\"}}")
    List<CodeInfo> findByDescriptionContaining(String description);

    // @Query("{\"query_string\": {\"query\": \"?0~\"}}")
    // List<CodeInfo> findByDescriptionFuzzy(String query);



    @Query("{\"bool\": {\"must\": [{\"query_string\": {\"query\": \"?0~\"}}, {\"match\": {\"version\": \"?1\"}}]}}")
    List<CodeInfo> findByDescriptionFuzzyWithVersion(String word, String version);
}




