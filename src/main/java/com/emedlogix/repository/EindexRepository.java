package com.emedlogix.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.Eindex;

@Repository
public interface EindexRepository extends JpaRepository<Eindex, Integer> {
    @Query("SELECT e from Eindex e where e.code = :code and e.version = :version")
    List<Eindex> findMainTermBySearch(@Param("code") String code, @Param("version") String version);

    @Query(value = "SELECT t.parent_id as id,e.title as title,t.level as level,e.code as code,e.see as see,e.seealso as seealso,e.nemod as nemod,e.ismainterm as ismainterm from eindex e "
            + "join term_hierarchy t on t.parent_id=e.id where t.child_id = :id order by t.level asc", nativeQuery = true)
    List<Map<String, Object>> getParentChildList(Integer id);

    @Query(value = "SELECT e.id as id, e.title as title, e.code as code, e.see as see, e.seealso as seealso, e.seecat as seecat, e.nemod as nemod, e.ismainterm as ismainterm, t.child_id as childId, t.level as level FROM eindex e "
            + "JOIN term_hierarchy t ON t.child_id = e.id "
            + "WHERE t.parent_id IN (SELECT id FROM eindex WHERE title LIKE ?1% AND ismainterm = true AND version = ?2 ORDER BY title) "
            + "AND t.level IN (0, 1) AND e.version = ?2 ORDER BY e.title ASC", nativeQuery = true)
    List<Map<String, Object>> searchMainTermLevelOne(String title, String version);

    @Query(value = "SELECT e.id as id, e.title as title, e.code as code, e.see as see, e.seealso as seealso, e.seecat as seecat, e.nemod as nemod, e.ismainterm as ismainterm, t.parent_id as parentId, t.child_id as childId, t.level as level "
            + "FROM eindex e "
            + "JOIN term_hierarchy t ON t.parent_id = e.id "
            + "WHERE t.child_id IN (SELECT id FROM eindex WHERE title = :title AND ismainterm = false AND version = :version) "
            + "ORDER BY t.child_id, t.level", nativeQuery = true)
    List<Map<String, Object>> searchLevelTermMainTerm(@Param("title") String title, @Param("version") String version);

    @Query(value = "SELECT e.* from eindex e where e.title like ?1% and ismainterm=true", nativeQuery = true)
    List<Eindex> findMainTerm(String title);

    @Query(value = "SELECT e.* FROM eindex e WHERE e.title LIKE ?1% AND ismainterm = true AND version = ?2", nativeQuery = true)
    List<Eindex> findMainTermByNameAndVersion(String title, String version);

    @Query(value = "SELECT e.* FROM eindex e WHERE e.title LIKE ?1% AND ismainterm = true AND e.version = ?2", nativeQuery = true)
    List<Eindex> findMainTermByTitleAndVersion(String title, String version);

    @Query(value = "SELECT e.* from eindex e join term_hierarchy t on t.child_id=e.id where t.parent_id in (select id from eindex where title in(?1) and ismainterm=true) and e.title like ?2%", nativeQuery = true)
    List<Eindex> findSecondMainTermLevel(List<String> firstMainTerms, String title);

    @Query(value = "SELECT count(*) from eindex e join term_hierarchy t on t.child_id=e.id where t.parent_id=(select id from eindex where title=?1 and ismainterm=true) and e.title like ?2%", nativeQuery = true)
    Integer mainTermHasLevelTerm(String mainTerm, String levelTerm);

    List<Eindex> findByTitleStartingWithAndVersion(String filterBy, String version);

}
