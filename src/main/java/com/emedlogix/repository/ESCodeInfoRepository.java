package com.emedlogix.repository;


import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.CodeInfo;

@Repository
public interface ESCodeInfoRepository extends ElasticsearchRepository<CodeInfo, String>  {

    CodeInfo getByCode(String code);

    List<CodeInfo> findByCode(String s);
    List<CodeInfo> findByCodeStartingWith(String code);


    List<CodeInfo> findByDescriptionContains(String description);
}




