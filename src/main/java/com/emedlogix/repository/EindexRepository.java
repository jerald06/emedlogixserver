package com.emedlogix.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.Eindex;

@Repository
public interface EindexRepository extends JpaRepository<Eindex, Integer> {

	@Query("SELECT e from eindex e where e.code = :code")
	List<Eindex> findMainTermBySearch(String code);
	
	@Query(value = "SELECT t.parent_id as id,e.title as title,t.level as level,e.code as code,e.see as see,e.seealso as seealso,e.nemod as nemod,e.ismainterm as ismainterm from eindex e "
			+ "join term_hierarchy t on t.parent_id=e.id where t.child_id = :id order by t.level asc", nativeQuery = true)
	List<Map<String,Object>> getParentChildList(Integer id);

	@Query(value = "select e.id as id,e.title as title,e.code as code,e.see as see,e.seealso as seealso,e.seecat as seecat,e.nemod as nemod,e.ismainterm as ismainterm from eindex e "
			+ "join term_hierarchy t on t.child_id=e.id "
			+ "where t.parent_id in (select id from eindex where title like ':title%' and ismainterm=true order by title) "
			+ "and t.level=1 and e.code is not null order by e.title asc", nativeQuery = true)
	List<Map<String,Object>> searchMainTermLevelOne(String title);

	@Query(value = "select e.id as id,e.title as title,e.code as code,e.see as see,e.seealso as seealso,e.seecat as seecat,e.nemod as nemod,e.ismainterm as ismainterm,t.parent_id as parentId,t.child_id as childId,t.level as level "
			+ "from eindex e "
			+ "join term_hierarchy t on t.parent_id=e.id "
			+ "where t.child_id in (select id from eindex where title like ':title%' and ismainterm=false)", nativeQuery = true)
	List<Map<String,Object>> searchLevelTermMainTerm(String title);
	
	@Query(value="SELECT e from eindex e where e.title = :title% and ismainterm=true",nativeQuery = true)
	List<Eindex> findMainTerm(String title);
	
	@Query(value="SELECT e from eindex e join term_hierarchy t on t.child_id=e.id where t.parent_id in (select id from eindex where title in(:firstMainTerms) and ismainterm=true) and e.title = :title%",nativeQuery = true)
	List<Eindex> findSecondMainTermLevel(List<String> firstMainTerms, String title);
}
