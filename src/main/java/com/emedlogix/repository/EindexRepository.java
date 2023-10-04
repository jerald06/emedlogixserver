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
	List<Map<String,Object>> getParentChildList(Integer id);

	@Query(value = "select e.id as id,e.title as title,e.code as code,e.see as see,e.seealso as seealso,e.seecat as seecat,e.nemod as nemod,e.ismainterm as ismainterm,t.child_id as childId, t.level as level from eindex e "
			+ "join term_hierarchy t on t.child_id=e.id "
			+ "where t.parent_id in (select id from eindex where title like ?1% and ismainterm=true order by title) "
			+ "and t.level in (0,1) order by e.title asc", nativeQuery = true)
	List<Map<String,Object>> searchMainTermLevelOne(String title);

	@Query(value = "select e.id as id,e.title as title,e.code as code,e.see as see,e.seealso as seealso,e.seecat as seecat,e.nemod as nemod,e.ismainterm as ismainterm,t.parent_id as parentId,t.child_id as childId,t.level as level "
			+ "from eindex e "
			+ "join term_hierarchy t on t.parent_id=e.id "
			+ "where t.child_id in (select id from eindex where title=:title and ismainterm=false) order by t.child_id,t.level", nativeQuery = true)
	List<Map<String,Object>> searchLevelTermMainTerm(String title);

	@Query(value="SELECT e.* from eindex e where e.title like ?1% and ismainterm=true",nativeQuery = true)
	List<Eindex> findMainTerm(String title);

	@Query(value="SELECT e.* from eindex e join term_hierarchy t on t.child_id=e.id where t.parent_id in (select id from eindex where title in(?1) and ismainterm=true) and e.title like ?2%",nativeQuery = true)
	List<Eindex> findSecondMainTermLevel(List<String> firstMainTerms, String title);


	@Query(value="SELECT count(*) from eindex e join term_hierarchy t on t.child_id=e.id where t.parent_id=(select id from eindex where title=?1 and ismainterm=true) and e.title like ?2%",nativeQuery = true)
	Integer mainTermHasLevelTerm(String mainTerm, String levelTerm);

	List<Eindex> findByTitleStartingWith(String filterBy);


	@Query(value = "SELECT t.parent_id as id,e.title as title,t.level as level,e.code as code,e.see as see,e.seealso as seealso,e.nemod as nemod,e.ismainterm as ismainterm from eindex e \n" +
			"join term_hierarchy t on t.child_id=e.id where t.parent_id = :id and e.ismainterm= :ismainterm ", nativeQuery = true)
	List<Map<String,Object>> getIndexLevelTerms(Integer id,Boolean ismainterm);

	@Query(value = "SELECT t.parent_id as id,e.title as title,t.level as level,e.code as code,e.see as see,e.seealso as seealso,e.nemod as nemod,e.ismainterm as ismainterm from eindex e \n" +
			"join term_hierarchy t on t.parent_id=e.id where t.child_id = :id and e.ismainterm= :ismainterm ", nativeQuery = true)
	List<Map<String,Object>> getIndexMainTerms(Integer id,Boolean ismainterm);
}
