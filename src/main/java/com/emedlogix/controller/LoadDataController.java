package com.emedlogix.controller;


import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emedlogix.entity.FileStatus;


@RestController
@RequestMapping(value = "/load")
public interface LoadDataController {

    @PostMapping("/{filetype}/{year}")
    Map<String, Object> loadData(@PathVariable String filetype,@PathVariable Integer year,@RequestParam(required = true,value = "fileName") String fileName);
    
    @GetMapping("/{filetype}/{year}")
    FileStatus getFileSatus(@PathVariable String filetype,@PathVariable Integer year,@RequestParam(required = true,value = "fileName") String fileName);
}
