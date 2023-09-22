package com.emedlogix.repository;

import com.emedlogix.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, String> {
}
