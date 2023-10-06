package com.emedlogix.controller;

import com.emedlogix.entity.EindexVO;
import com.emedlogix.service.CodeSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/codes")
public class CombinedCodeSearchController {

    @Autowired
    private CodeSearchService codeSearchService;

    @GetMapping("/index/search/combined")
    public List<EindexVO> getCombinedEIndexSearch(@RequestParam(required = true, value = "name") String name,
                                                  @RequestParam(required = false, value = "mainTermSearch", defaultValue = "true") boolean mainTermSearch,
                                                  @RequestParam("version") String version) {

        List<EindexVO> result = new ArrayList<>();

        // Call the first API and add its results to the combined result
        List<EindexVO> resultFromApi1 = codeSearchService.getEIndexByNameSearch(name, mainTermSearch,version);
        result.addAll(resultFromApi1);

        // Call the second API and add its results to the combined result
        List<EindexVO> resultFromApi2 = codeSearchService.getEIndexByTermSearch(name, mainTermSearch,version);
        result.addAll(resultFromApi2);

        // Deduplicate the results based on the 'id' field
        result = deduplicateById(result);

        return result;
    }
    private List<EindexVO> deduplicateById(List<EindexVO> resultList) {
        // Create a map to track unique items by 'id'
        Map<Integer, EindexVO> uniqueItems = new HashMap<>();

        for (EindexVO item : resultList) {
            // Check if the item's 'id' is not in the map, add it as a unique item
            if (!uniqueItems.containsKey(item.getId())) {
                uniqueItems.put(item.getId(), item);
            } else {
                // If the item's 'id' is already in the map, update it with non-null values
                EindexVO existingItem = uniqueItems.get(item.getId());
                if (item.getDerivedCode() != null) {
                    // Update both 'code' and 'derivedCode' fields
                    existingItem.setCode(item.getDerivedCode());
                    existingItem.setDerivedCode(item.getDerivedCode());
                }
                if (item.getType() != null) {
                    existingItem.setType(item.getType());
                }
                // You can add other fields to update if needed
            }
        }

        // Convert the map of unique items back to a list
        List<EindexVO> deduplicatedResult = new ArrayList<>(uniqueItems.values());

        return deduplicatedResult;
    }

}

