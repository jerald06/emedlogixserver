package com.emedlogix.controller;


import java.io.IOException;
import java.util.List;

import com.emedlogix.entity.*;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emedlogix.entity.CodeDetails;
import com.emedlogix.entity.CodeInfo;
import com.emedlogix.entity.Eindex;
import com.emedlogix.entity.EindexVO;
import com.emedlogix.entity.MedicalCodeVO;


@RestController
@RequestMapping(value = "/codes")
public interface CodeSearchController {

    @GetMapping("/{code}/search")
    CodeInfo getCodeInfo(@PathVariable String code) throws IOException;

    @GetMapping("/{code}/matches")
    List<CodeInfo> getCodeInfoMatches(@PathVariable String code,@RequestParam("version") String version);

    @GetMapping("/{description}/description")
    List<CodeInfo> getCodeInfoDescription(@PathVariable String description,@RequestParam("version") String version);

    @GetMapping("/{code}/details")
    CodeDetails getCodeInfoDetails(@PathVariable String code, @RequestParam("version") String version);

    @GetMapping("/{code}/index")
    List<EindexVO> getEIndex(@PathVariable String code,@RequestParam("version") String version);

    @GetMapping("/alldetails/index")
    List<Eindex> getIndexDetails();

    @GetMapping("/description")
    List<CodeInfo> getDescriptionDetails(@RequestParam("keywords") String sortBy);

    @GetMapping("/alldetails/index/title")
    List<Eindex> getIndexDetailsByTitleStartingWith(@RequestParam String filterBy,@RequestParam String version);

    @GetMapping("/{code}/neoplasm")
    List<MedicalCodeVO> getNeoPlasm(@PathVariable String code,@RequestParam("version") String version);

    @GetMapping("/alldetails/neoplasm")
    List<MedicalCodeVO> getNeoplasmDetails(@RequestParam(value = "title", required = false) String title,
                                           @RequestParam(value = "version",required = false) String version);

    @GetMapping("/filterby/neoplasm")
    List<MedicalCodeVO> filterNeoplasmDetails(@RequestParam("filterBy") String filterBy);

    @GetMapping("/{code}/drug")
	List<MedicalCodeVO> getDrug(@PathVariable String code,@RequestParam("version")String version);

    @GetMapping("/alldetails/drug")
    List<MedicalCodeVO> getDrugDetails(@RequestParam(value = "title", required = false) String title,@RequestParam("version")String version);


    @GetMapping("/index/search/name")
    List<EindexVO> getEIndexByNameSearch(@RequestParam(required = true,value = "name") String name,
                                         @RequestParam(required = false,value = "mainTermSearch",defaultValue = "true") boolean mainTermSearch,
                                         @RequestParam("version") String version);

    @GetMapping("/alter-terms/search")
    List<AlterTerm> searchByAlterDescription(@RequestParam String alterDescription,@RequestParam String version);

    @GetMapping("/index/search/term")
    List<EindexVO> getEIndexByTermSearch(@RequestParam(required = true, value = "name") String name,
                                         @RequestParam(required = false, value = "mainTermSearch", defaultValue = "true") boolean mainTermSearch,
                                         @RequestParam("version") String version);

}
