
package com.emedlogix.repository;

import com.emedlogix.entity.EindexLevels;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ESIndexLevelSearchRepository extends ElasticsearchRepository<EindexLevels, String> {
    EindexLevels getByTitle(String title);
}

