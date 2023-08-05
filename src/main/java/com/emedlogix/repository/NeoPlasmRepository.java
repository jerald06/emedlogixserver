package com.emedlogix.repository;

import java.util.Map;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.Neoplasm;

@Repository
public interface NeoPlasmRepository extends JpaRepository<Neoplasm, Integer> {

	@Query(value = "SELECT n.id as id ,n.title as title,n.see as see,n.seealso as seealso,n.nemod as nemod,n.ismainterm as ismainterm,GROUP_CONCAT(c.code) as code "
			+ "FROM neoplasm n join neoplasm_code c on n.id=c.neoplasm_id "
			+ "WHERE c.neoplasm_id in (select neoplasm_id from neoplasm_code where  code= :code) group by n.id", nativeQuery = true)
	List<Map<String,Object>> findNeoplasmByCode(String code);
	
	@Query(value = "SELECT t.parent_id as id,n.title as title,n.nemod as nemod,t.level as level,n.ismainterm as ismainterm "
			+ "FROM neoplasm n join neoplasm_hierarchy t on t.parent_id=n.id where t.child_id = :id order by t.level asc", nativeQuery = true)
	List<Map<String,Object>> getParentChildList(Integer id);

	@Query(value = "SELECT n.id as id, n.title as title, n.see as see, n.seealso as seealso, n.nemod as nemod, n.ismainterm as ismainterm, GROUP_CONCAT(c.code) as code "
			+ "FROM neoplasm n join neoplasm_code c on n.id=c.neoplasm_id "
			+ "group by n.id", nativeQuery = true)
	List<Map<String, Object>> findAllNeoplasmData();

	@Query(value = "SELECT * FROM neoplasm WHERE title LIKE :title%", nativeQuery = true)
	List<Map<String, Object>> filterNeoplasmData(@Param("title") String title);


}
