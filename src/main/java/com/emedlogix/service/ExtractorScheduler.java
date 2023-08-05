package com.emedlogix.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtractorScheduler {
    public static final Logger logger = LoggerFactory.getLogger(ExtractorScheduler.class);

    @Autowired
    ExtractorService extractorService;

    @PostConstruct
    public void doExtractChaperSectionXML() {
        logger.info("Extractor Service has been initiated for XML extraction...");
       // extractorService.doExtractCapterSectionXML();
        logger.info("Extractor Service XML completed...");
    }

    @PostConstruct
    public void doExtractOderCode() {
        logger.info("Extractor Service has been initiated for Ordered Codes...");
       // extractorService.doExtractOrderedCodes();
        logger.info("Extractor Service Ordered Codes completed...");
    }
    
    @PostConstruct
    public void doExtractIndex() {
        logger.info("Extractor Service has been initiated for Eindex Codes...");
       //extractorService.doExtractIndex();
        logger.info("Extractor Service Eindex Codes completed...");
    }
    
    @PostConstruct
    public void doExtractNeoplasm() {
        logger.info("Extractor Service has been initiated for Neoplasm Codes...");
       // extractorService.doExtractNeoplasm();
        logger.info("Extractor Service Neoplasm Codes completed...");
    }
    
    @PostConstruct
    public void doExtractDrug() {
        logger.info("Extractor Service has been initiated for Drug Codes...");
        //extractorService.doExtractDrug();
        logger.info("Extractor Service Drug Codes completed...");
    }
}
