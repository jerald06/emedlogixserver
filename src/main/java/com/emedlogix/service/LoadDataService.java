package com.emedlogix.service;

import static com.emedlogix.util.Constants.DRUG;
import static com.emedlogix.util.Constants.NEOPLASM;
import static com.emedlogix.util.Constants.INDEX;
import static com.emedlogix.util.Constants.TABULAR;
import static com.emedlogix.util.Constants.ORDER;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emedlogix.controller.LoadDataController;
import com.emedlogix.entity.FileStatus;
import com.emedlogix.repository.FileStatusReposistory;

@Service
public class LoadDataService implements LoadDataController {
	
    @Autowired
    ExtractorService extractorService;
    
    @Autowired
    FileStatusReposistory fileStatusReposistory;

	@Override
	public Map<String, Object> loadData(String filetype, Integer year, String fileName) {
		Map<String,Object> result = new HashMap<>();
		Thread thread = new Thread(new Runnable() {
		    public void run() {
		       loadFiles(filetype,year,fileName);
		    }
		});
		thread.start();
		result.put("Status", "Success");
		return result;
	}

	protected void loadFiles(String fileType, Integer year, String fileName) {
		if(fileType.equalsIgnoreCase(INDEX)) {
			extractorService.loadIndexData(year,fileName);
		}
		if(fileType.equalsIgnoreCase(DRUG)) {
			extractorService.loadDrugData(year,fileName);
		}
		if(fileType.equalsIgnoreCase(NEOPLASM)) {
			extractorService.loadNeoplasmData(year,fileName);
		}
		if(fileType.equalsIgnoreCase(TABULAR)) {
			extractorService.loadCapterSectionData(year,fileName);
		}
		if(fileType.equalsIgnoreCase(ORDER)) {
			extractorService.loadOrderedCodesData(year,fileName);
		}
	}

	@Override
	public FileStatus getFileSatus(String filetype, Integer year, String fileName) {
		return fileStatusReposistory.getFileStatus(filetype, year, fileName);
	}
}
