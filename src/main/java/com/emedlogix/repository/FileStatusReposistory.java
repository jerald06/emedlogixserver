package com.emedlogix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.FileStatus;

@Repository
public interface FileStatusReposistory extends JpaRepository<FileStatus, Integer> {

	@Query("SELECT f from file_status f where f.fileType=?1 and f.year=?2 and f.fileName=?3 and f.version=?4")
	FileStatus findFileStatus(String fileType,Integer year,String fileName,String version);
	
	@Query("SELECT f from file_status f where f.fileType=?1 and f.year=?2 and f.fileName=?3")
	FileStatus getFileStatus(String fileType,Integer year,String fileName);
}
