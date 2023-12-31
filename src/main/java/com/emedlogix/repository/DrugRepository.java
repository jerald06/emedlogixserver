package com.emedlogix.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.emedlogix.entity.Drug;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Integer> {

    @Query(value = "SELECT n.id as id, n.title as title, n.see as see, n.seealso as seealso, n.nemod as nemod, n.ismainterm as ismainterm, GROUP_CONCAT(c.code) as code "
            + "FROM drug n JOIN drug_code c ON n.id = c.drug_id "
            + "WHERE c.drug_id IN (SELECT drug_id FROM drug_code WHERE code = :code) "
            + "AND n.version = :version GROUP BY n.id", nativeQuery = true)
    List<Map<String, Object>> findDrugByCodeAndVersion(@Param("code") String code, @Param("version") String version);

    @Query(value = "SELECT t.parent_id as id,n.title as title,n.nemod as nemod,t.level as level,n.ismainterm as ismainterm "
            + "FROM drug n join drug_hierarchy t on t.parent_id=n.id where t.child_id = :id order by t.level asc", nativeQuery = true)
    List<Map<String, Object>> getParentChildList(Integer id);

    @Query(value = "SELECT n.id as id ,n.title as title,n.see as see,n.seealso as seealso,n.nemod as nemod,n.ismainterm as ismainterm,GROUP_CONCAT(c.code) as code "
            + "FROM drug n join drug_code c on n.id=c.drug_id "
            + "group by n.id", nativeQuery = true)
    List<Map<String, Object>> findAllDrugData();

    @Query(value = "SELECT n.id as id, n.title as title, n.see as see, n.seealso as seealso, n.nemod as nemod, n.ismainterm as ismainterm, GROUP_CONCAT(c.code) as code, n.version as version "
            + "FROM drug n join drug_code c on n.id=c.drug_id "
            + "WHERE n.version = :version "
            + "GROUP BY n.id", nativeQuery = true)
    List<Map<String, Object>> findAllDrugDataByVersion(@Param("version") String version);

    @Query(value = "SELECT n.id as id, n.title as title, n.see as see, n.seealso as seealso, n.nemod as nemod, n.ismainterm as ismainterm, GROUP_CONCAT(c.code) as code, n.version as version "
            + "FROM drug n JOIN drug_code c ON n.id=c.drug_id "
            + "WHERE n.title REGEXP :titlePattern AND n.version = :version "
            + "GROUP BY n.id "
            + "ORDER BY n.title", nativeQuery = true)
    List<Map<String, Object>> findDrugByTitleAndVersion(@Param("titlePattern") String titlePattern,
                                                        @Param("version") String version);

}
