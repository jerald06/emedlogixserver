package com.emedlogix.service;


public interface ExtractorService {

    void doExtractCapterSectionXML();

    void doExtractOrderedCodes();
    
    void doExtractIndex();
    
    void doExtractNeoplasm();
    
    void doExtractDrug();

    void doExtractAlternateTermXLSX();

	void loadIndexData(Integer year,String fileName);

	void loadNeoplasmData(Integer year,String fileName);

	void loadDrugData(Integer year,String fileName);

	void loadCapterSectionData(Integer year, String fileName);

	void loadOrderedCodesData(Integer year, String fileName);
    void loadAlternateTermsData(Integer year, String fileName);

}
