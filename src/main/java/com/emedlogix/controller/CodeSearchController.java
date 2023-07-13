package com.emedlogix.controller;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emedlogix.entity.CodeDetails;
import com.emedlogix.entity.CodeInfo;
import com.emedlogix.entity.EIndex;


@RestController
@RequestMapping(value = "/codes")
public interface CodeSearchController {

    @GetMapping("/{code}/search")
    CodeInfo getCodeInfo(@PathVariable String code) throws IOException;
    @GetMapping("/{code}/matches")
    List<CodeInfo> getCodeInfoMatches(@PathVariable String code);
    @GetMapping("/{code}/details")
    CodeDetails getCodeInfoDetails(@PathVariable String code);
    @GetMapping("/{code}/index")
    List<EIndex> getEIndex(@PathVariable String code, @RequestParam("filterBy") String filterBy);
    @GetMapping("/{code}/neoplasm")
    List<Map<String,Object>> getNeoPlasm(@PathVariable String code);
    @GetMapping("/{code}/drug")
	List<Map<String, Object>> getDrug(@PathVariable String code);
}
