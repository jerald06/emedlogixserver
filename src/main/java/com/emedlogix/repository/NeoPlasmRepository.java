package com.emedlogix.repository;

import com.emedlogix.entity.Neoplasm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NeoPlasmRepository extends JpaRepository<Neoplasm, Integer> {

	@Query(value = "SELECT n.id as id, n.title as title, n.see as see, n.seealso as seealso, n.nemod as nemod, n.ismainterm as ismainterm, GROUP_CONCAT(c.code) as code "
			+ "FROM neoplasm n JOIN neoplasm_code c ON n.id = c.neoplasm_id "
			+ "WHERE c.neoplasm_id IN (SELECT neoplasm_id FROM neoplasm_code WHERE code = :code) "
			+ "AND n.version = :version GROUP BY n.id", nativeQuery = true)
	List<Map<String, Object>> findNeoplasmByCodeAndVersion(@Param("code") String code, @Param("version") String version);

	@Query(value = "SELECT t.parent_id as id,n.title as title,n.nemod as nemod,t.level as level,n.ismainterm as ismainterm "
			+ "FROM neoplasm n join neoplasm_hierarchy t on t.parent_id=n.id where t.child_id = :id order by t.level asc", nativeQuery = true)
	List<Map<String,Object>> getParentChildList(Integer id);

	@Query(value = "SELECT n.id as id, n.title as title, n.see as see, n.seealso as seealso, n.nemod as nemod, n.ismainterm as ismainterm, GROUP_CONCAT(c.code) as code, n.version as version "
			+ "FROM neoplasm n join neoplasm_code c on n.id=c.neoplasm_id "
			+ "WHERE n.version = :version "
			+ "GROUP BY n.id", nativeQuery = true)
	List<Map<String, Object>> findAllNeoplasmDataByVersion(@Param("version") String version);

	@Query(value = "SELECT * FROM neoplasm WHERE title LIKE :title%", nativeQuery = true)
	List<Map<String, Object>> filterNeoplasmData(@Param("title") String title);


	@Query(value = "SELECT n.id as id, n.title as title, n.see as see, n.seealso as seealso, n.nemod as nemod, n.ismainterm as ismainterm, GROUP_CONCAT(c.code) as code, n.version as version "
			+ "FROM neoplasm n JOIN neoplasm_code c ON n.id=c.neoplasm_id "
			+ "WHERE n.title REGEXP :titlePattern AND n.version = :version "
			+ "GROUP BY n.id "
			+ "ORDER BY n.title", nativeQuery = true)
	List<Map<String, Object>> findNeoplasmDataByTitleAndVersion(
			@Param("titlePattern") String titlePattern,
			@Param("version") String version
	);


}