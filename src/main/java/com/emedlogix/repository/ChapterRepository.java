package com.emedlogix.repository;

import com.emedlogix.entity.Chapter;
import com.emedlogix.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, String> {

    Chapter findFirstByNameAndVersion(String name,String version);
}
