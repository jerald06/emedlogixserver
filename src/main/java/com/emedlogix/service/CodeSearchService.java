package com.emedlogix.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.*;
import java.util.stream.Collectors;

import com.emedlogix.entity.*;
import com.emedlogix.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.emedlogix.controller.CodeSearchController;
import com.emedlogix.entity.CodeDetails;
import com.emedlogix.entity.CodeInfo;
import com.emedlogix.entity.Eindex;
import com.emedlogix.entity.EindexVO;
import com.emedlogix.entity.MedicalCodeVO;
import com.emedlogix.entity.Section;
import com.emedlogix.repository.ChapterRepository;
import com.emedlogix.repository.DBCodeDetailsRepository;
import com.emedlogix.repository.DrugRepository;
import com.emedlogix.repository.ESCodeInfoRepository;
import com.emedlogix.repository.EindexRepository;

import com.emedlogix.repository.SectionRepository;
import org.springframework.web.bind.annotation.RequestParam;


@Service
public class CodeSearchService implements CodeSearchController {

    public static final Logger logger = LoggerFactory.getLogger(CodeSearchService.class);
    private static final String INDEX_NAME = "details";
    private static final String FIELD_NAME = "code";
    private Map<String, Object> indexMap = null;
    private String code = null;

    @Autowired
    ESCodeInfoRepository esCodeInfoRepository;

    @Autowired
    DBCodeDetailsRepository dbCodeDetailsRepository;

    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    ChapterRepository chapterRepository;

    @Autowired
    NeoPlasmRepository neoPlasmRepository;

    @Autowired
    DrugRepository drugRepository;

    @Autowired
    EindexRepository eindexRepository;

    @Autowired
    ESAlterTermRepository esAlterTermRepository;

    @Override
    public CodeInfo getCodeInfo(String code) {
        logger.info("Getting Code Information for:", code);
        CodeInfo codeInfo = esCodeInfoRepository.getByCode(code);
        return codeInfo;
    }

    public List<CodeInfo> getCodeInfoMatches(String code,String version) {
        logger.info("Getting Code Information for code starts with:", code);
        List<CodeInfo> codeInfoList = new ArrayList<>();
        Iterable<CodeInfo> codeDetailsIterable = esCodeInfoRepository.findByCodeStartingWithAndVersion(code,version);
        Iterator<CodeInfo> it = codeDetailsIterable.iterator();
        while (it.hasNext()) {
            CodeInfo codeInfo = it.next();
            codeInfoList.add(codeInfo);
        }
        logger.info("Got matching codes size:", codeInfoList.size());
        Collections.sort(codeInfoList, new Comparator<CodeInfo>() {
            @Override
            public int compare(CodeInfo codeInfo1, CodeInfo codeInfo2) {
                return codeInfo1.getCode().compareTo(codeInfo2.getCode());
            }
        });
        return codeInfoList;
    }

    @Override
    public List<CodeInfo> getCodeInfoDescription(String description,String version) {
        logger.info("Getting Code Information for Description: {}", description);

        // Use the findByDescriptionExactMatchWithVersion method
        List<CodeInfo> exactMatches = esCodeInfoRepository.findByDescriptionExactMatchWithVersion(description, version);

        // If there are exact matches, return them immediately
        if (!exactMatches.isEmpty()) {
            logger.info("Got exact matches: {}", exactMatches.size());
            return exactMatches;
        }

        // If there are no exact matches, perform a fuzzy search
        String[] words = description.split("\\s+");
        List<CodeInfo> codeInfoList = new ArrayList<>();
        for (String word : words) {
            List<CodeInfo> wordMatches = esCodeInfoRepository.findByDescriptionExactMatchWithVersion(word, version);
            codeInfoList.addAll(wordMatches);
        }

        logger.info("Got matching descriptions (including fuzzy): {}", codeInfoList.size());

        // Sort the combined list by code
        Collections.sort(codeInfoList, new Comparator<CodeInfo>() {
            @Override
            public int compare(CodeInfo codeInfo1, CodeInfo codeInfo2) {
                return codeInfo1.getCode().compareTo(codeInfo2.getCode());
            }
        });

        return codeInfoList;
    }

    public CodeDetails getCodeInfoDetails(@PathVariable String code, @RequestParam String version) {
        logger.info("Getting Code Information Details for code:", code);
        CodeDetails codeDetails = dbCodeDetailsRepository.findFirstByCodeAndVersion(code,version);
        Section section = sectionRepository.findFirstByCodeAndVersion(code, version);
        if (section != null) {
            codeDetails.setSection(section);
          Chapter chapter =   chapterRepository.findFirstByNameAndVersion(section.getChapterId(),section.getVersion());
            if (chapter != null){
                codeDetails.setChapter(chapter);
            }
        }
        return codeDetails;
    }

    @Override
    public List<EindexVO> getEIndex(String code,String version) {
        return eindexRepository.findMainTermBySearch(code,version).stream().map(m -> {
            return getParentChildHierarchy(m);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Eindex> getIndexDetails() {
        return eindexRepository.findAll();
    }

    @Override
    public List<CodeInfo> getDescriptionDetails(String keywords) {
        logger.info("Getting Code information Details for Keywords: {}", keywords);
        List<CodeInfo> codeInfoList = new ArrayList<>();
        String[] keywordArray = keywords.split(" ");
        for (String keyword : keywordArray) {
            Iterable<CodeInfo> codeInfoIterable = esCodeInfoRepository.findByDescriptionContaining(keyword.trim());
            codeInfoIterable.forEach(codeInfoList::add);
        }
        logger.info("Got description size: {}", codeInfoList.size());
        return codeInfoList;
    }

    @Override
    public List<Eindex> getIndexDetailsByTitleStartingWith(String filterBy,String version) {
        return eindexRepository.findByTitleStartingWithAndVersion(filterBy,version);
    }


    @Override
    public List<MedicalCodeVO> getNeoPlasm(String code,String version) {
        return neoPlasmRepository.findNeoplasmByCodeAndVersion(code,version).stream().map(m -> {
            return getDrugNeoplasmHierarchy(m, "neoplasm");
        }).collect(Collectors.toList());
    }


    @Override
    public List<MedicalCodeVO> getNeoplasmDetails(String title,String version) {
        List<Map<String, Object>> allNeoplasmData;
        if (title != null && !title.isEmpty()) {
            String titlePattern = "^" + title.toLowerCase();
            allNeoplasmData = neoPlasmRepository.findNeoplasmDataByTitleAndVersion(titlePattern,version);
        } else {
            allNeoplasmData = neoPlasmRepository.findAllNeoplasmDataByVersion(version);
        }
        return allNeoplasmData.stream().map(this::populateMedicalCode).collect(Collectors.toList());

    }


    @Override
    public List<MedicalCodeVO> filterNeoplasmDetails(String filterBy) {
        List<Map<String, Object>> allNeoplasmData = neoPlasmRepository.filterNeoplasmData(filterBy);
        return allNeoplasmData.stream().map(m -> {
            return populateMedicalCode(m);
        }).collect(Collectors.toList());
    }


    @Override
    public List<MedicalCodeVO> getDrug(String code,String version) {
        return drugRepository.findDrugByCodeAndVersion(code,version).stream().map(m -> {
            return getDrugNeoplasmHierarchy(m, "drug");
        }).collect(Collectors.toList());
    }

    public List<MedicalCodeVO> getDrugDetails(String title,String version) {
        List<Map<String, Object>> allDrugData;
        if (title != null && !title.isEmpty()) {
            String titlePattern = "^" + title.toLowerCase();
            allDrugData = drugRepository.findDrugByTitleAndVersion(titlePattern,version);
        } else {
            allDrugData = drugRepository.findAllDrugDataByVersion(version);
        }
        return allDrugData.stream().map(this::populateMedicalCode).collect(Collectors.toList());
    }

    @Override
    public List<EindexVO> getEIndexByNameSearch(String name, boolean mainTermSearch,String version) {
        String[] names = name.trim().split(" ");
        if (names.length > 1 && names.length == 2) {
            return multipleSearch(names, mainTermSearch,version);
        } else {
            if (mainTermSearch) {
                return singleMainTermSearch(names[0],version);
            } else {
                return singleLevelTermSearch(names[0],version);
            }
        }
    }

    @Override
    public List<AlterTerm> searchByAlterDescription(String alterDescription,String version) {
        return esAlterTermRepository.findByAlterDescriptionAndVersion(alterDescription,version);
    }


    @Override
    public List<EindexVO> getEIndexByTermSearch(String name, boolean mainTermSearch,String version) {
        String[] names = name.trim().split(" ");
        List<EindexVO> result = new ArrayList<>();
        if (names.length == 1) {
            result = singleTermSearch(names[0],version);
        } else if (names.length == 2) {
            result = multipleSearch(names, mainTermSearch,version);
        }
        return result;
    }

    private List<EindexVO> singleTermSearch(String name,String version) {
        List<EindexVO> result = new ArrayList<>();
        // Search for the given term
        List<Eindex> termResults = eindexRepository.findMainTermByNameAndVersion(name,version);
        for (Eindex termResult : termResults) {
            EindexVO termVO = extractEindexVO(termResult);
            // Check if the term has no code but has see or seealso
            if (termVO.getCode() == null && (termVO.getSee() != null || termVO.getSeealso() != null)) {
                List<EindexVO> associatedCodes = findCodesForAssociatedTerms(termVO);
                if (!associatedCodes.isEmpty()) {
                    termVO.setCode(associatedCodes.get(0).getDerivedCode()); // Pass the derivedCode to code
                }
            }
            result.add(termVO); // Add the term to the result
        }
        return result;
    }

    private EindexVO extractEindexVO(Eindex i) {
        EindexVO eindexVO = new EindexVO();
        eindexVO.setId(i.getId());
        eindexVO.setTitle(i.getTitle());
        eindexVO.setCode(i.getCode());
        eindexVO.setSee(i.getSee());
        eindexVO.setSeealso(i.getSeealso());
        eindexVO.setIsmainterm(i.getIsmainterm());
        eindexVO.setNemod(i.getNemod());
        eindexVO.calculateType();

        // Check if ismainterm, code is null, and see or seealso is not null
        if (eindexVO.getIsmainterm() && eindexVO.getCode() == null && (eindexVO.getSee() != null || eindexVO.getSeealso() != null)) {
            List<String> associatedCodes = findAssociatedCodes(eindexVO);
            if (!associatedCodes.isEmpty()) {
                eindexVO.setDerivedCode(associatedCodes.get(0)); // Set the derived code value
            }
        }
        return eindexVO;
    }

    private List<EindexVO> findCodesForAssociatedTerms(EindexVO termVO) {
        List<EindexVO> result = new ArrayList<>();
        // Check if there is a "see" term
        if (termVO.getSee() != null) {
            List<Eindex> seeTerms = eindexRepository.findMainTerm(termVO.getSee());
            for (Eindex seeTerm : seeTerms) {
                if (seeTerm.getIsmainterm() && seeTerm.getCode() != null) {
                    termVO.setDerivedCode(seeTerm.getCode());
                    result.add(termVO);
                }
            }
        }
        // Check if there is a "seealso" term
        if (termVO.getSeealso() != null) {
            List<Eindex> seeAlsoTerms = eindexRepository.findMainTerm(termVO.getSeealso());
            for (Eindex seeAlsoTerm : seeAlsoTerms) {
                if (seeAlsoTerm.getIsmainterm() && seeAlsoTerm.getCode() != null) {
                    termVO.setDerivedCode(seeAlsoTerm.getCode());
                    result.add(termVO);
                }
            }
        }
        return result;
    }

    private List<String> findAssociatedCodes(EindexVO termVO) {
        List<String> associatedCodes = new ArrayList<>();

        if (termVO.getSee() != null) {
            String[] seeTerms = termVO.getSee().split(",");
            for (String seeTerm : seeTerms) {
                List<Eindex> codeResults = eindexRepository.findMainTerm(seeTerm.trim());
                for (Eindex codeResult : codeResults) {
                    if (codeResult.getIsmainterm() && codeResult.getCode() != null) {
                        associatedCodes.add(codeResult.getCode());
                    }
                }
            }
        }

        if (termVO.getSeealso() != null) {
            String[] seeAlsoTerms = termVO.getSeealso().split(",");
            for (String seeAlsoTerm : seeAlsoTerms) {
                List<Eindex> codeResults = eindexRepository.findMainTerm(seeAlsoTerm.trim());
                for (Eindex codeResult : codeResults) {
                    if (codeResult.getIsmainterm() && codeResult.getCode() != null) {
                        associatedCodes.add(codeResult.getCode());
                    }
                }
            }
        }

        return associatedCodes;
    }


    private List<EindexVO> multipleSearch(String[] names, boolean mainTermSearch,String version) {
        List<EindexVO> result = new ArrayList<>();
        if (mainTermSearch) {
            List<Eindex> mainTermResult = eindexRepository.findMainTermByTitleAndVersion(names[0],version);
            if (mainTermResult.size() == 0) {
                return result;
            }
            List<String> mainTermSeeSeeAlso = new ArrayList<>();
            mainTermResult.forEach(e -> {
                getMainTermSeeAndSeealso(e, mainTermSeeSeeAlso);
            });
            for (String searchTerm : names) {
                List<EindexVO> searchResult;
                if (mainTermSearch) {
                    searchResult = singleMainTermSearch(searchTerm,version);
                } else {
                    searchResult = singleLevelTermSearch(searchTerm,version);
                }
                result.addAll(searchResult);
            }
            //mainTerm mainTerm(See/See Also term of main term has 2nd main term)
            if (mainTermSeeSeeAlso.contains(names[1])) {
                return mainTermResult.stream().map(i -> {
                    return extractEintexVO(i);
                }).collect(Collectors.toList());
            }
            //mainTerm LevelTermof1stTerm
            Integer resultCount = eindexRepository.mainTermHasLevelTerm(names[0], names[1]);
            if (resultCount > 0) {
                result = singleMainTermSearch(names[0],version);
            }
            if (result.size() > 0) {
                return result;
            }
            //mainTerm NotLevelTermof1stTerm(Show if See/See Also term of main term have the level term entered)
            result = eindexRepository.findSecondMainTermLevel(mainTermSeeSeeAlso, names[1]).stream().map(i -> {
                return extractEintexVO(i);
            }).collect(Collectors.toList());
            if (result.size() > 0) {
                return result;
            }

            //mainTerm mainTerm(Else show all Level terms applicable for 2nd main term)
            result = singleMainTermSearch(names[1],version);
            if (result.size() > 0) {
                return result;
            }

            //mainTerm NotLevelTermof1stTerm(Else show all main terms associated with the level term entered)
            result = singleLevelTermSearch(names[1],version);
            if (result.size() > 0) {
                return result;
            }
            //mainTerm NotinIndextable(Else show all Level(1st level) terms applicable for 1st main term)
            result = singleMainTermSearch(names[0],version);
            if (result.size() > 0) {
                return result;
            }

        } else {
            //LevelTerm Maintermof1stterm
            Integer resultCount = eindexRepository.mainTermHasLevelTerm(names[1], names[0]);
            if (resultCount > 0) {
                result = singleMainTermSearch(names[1],version);
            }
            if (result.size() > 0) {
                return result;
            }
            //LevelTerm NotinIndextable
            result = singleLevelTermSearch(names[0],version);
            if (result.size() > 0) {
                return result;
            }
        }
        return result;
    }

    private EindexVO extractEintexVO(Eindex i) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.convertValue(i, new TypeReference<Map<String, Object>>() {
        });
        return populateEindexVO(map);
    }

    private void getMainTermSeeAndSeealso(Eindex eindex, List<String> mainTermsTitle) {
        if (eindex.getSee() != null) {
            mainTermsTitle.addAll(Arrays.asList(eindex.getSee().split(",")));
        }
        if (eindex.getSeealso() != null) {
            mainTermsTitle.addAll(Arrays.asList(eindex.getSeealso().split(",")));
        }
    }

//    private List<EindexVO> singleLevelTermSearch(String name,String version) {
//        List<EindexVO> indexList = new ArrayList<>();
//        eindexRepository.searchLevelTermMainTerm(name,version).forEach(map -> {
//            if (indexMap != null && Integer.parseInt(indexMap.get("childId").toString()) != Integer.parseInt(map.get("childId").toString())) {
//                indexList.add(populateEindexVO(indexMap, code));
//                code = null;
//            }
//            indexMap = map;
//            if (map.get("code") != null) {
//                code = map.get("code").toString();
//            }
//        });
//        indexList.add(populateEindexVO(indexMap, code));
//        List<EindexVO> resultIndex = indexList.stream().filter(distinctByKey(p -> p.getId()))
//                .collect(Collectors.toList());
//        resultIndex.sort(Comparator.comparing(m -> m.getTitle(),
//                Comparator.nullsLast(Comparator.naturalOrder())
//        ));
//        return resultIndex;
//    }
private List<EindexVO> singleLevelTermSearch(String name, String version) {
    List<EindexVO> indexList = new ArrayList<>();
    eindexRepository.searchLevelTermMainTerm(name, version).forEach(map -> {
        if (indexMap != null && Integer.parseInt(indexMap.get("childId").toString()) != Integer.parseInt(map.get("childId").toString())) {
            indexList.add(populateEindexVO(indexMap, code));
            code = null;
        }
        indexMap = map;
        if (map.get("code") != null) {
            code = map.get("code").toString();
        }
    });
    indexList.add(populateEindexVO(indexMap, code));
    List<EindexVO> resultIndex = indexList.stream().filter(distinctByKey(p -> p.getId()))
            .collect(Collectors.toList());
    resultIndex.sort(Comparator.comparing(m -> m.getTitle(),
            Comparator.nullsLast(Comparator.naturalOrder())
    ));
    return resultIndex;
}

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private List<EindexVO> singleMainTermSearch(String name,String version) {
        return eindexRepository.searchMainTermLevelOne(name,version).stream().map(m -> {
            return populateEindexVO(m);
        }).collect(Collectors.toList());
    }

    public List<MedicalCodeVO> getDrugDetails() {
        List<Map<String, Object>> allDrugData = drugRepository.findAllDrugData();
        return allDrugData.stream().map(m -> {
            return populateMedicalCode(m);
        }).collect(Collectors.toList());
    }

    private MedicalCodeVO getDrugNeoplasmHierarchy(Map<String, Object> m, String type) {
        MedicalCodeVO resultMedicalCode = null;
        List<Map<String, Object>> resultMap = new ArrayList<>();
        if (type == "neoplasm") {
            resultMap = neoPlasmRepository.getParentChildList(Integer.valueOf(String.valueOf(m.get("id"))));
        } else if (type == "drug") {
            resultMap = drugRepository.getParentChildList(Integer.valueOf(String.valueOf(m.get("id"))));
        }
        for (int x = 0; x < resultMap.size(); x++) {
            if (resultMedicalCode == null) {
                resultMedicalCode = populateMedicalCode(m);
            } else {
                MedicalCodeVO medicalCode = populateMedicalCode(resultMap.get(x));
                medicalCode.setChild(resultMedicalCode);
                resultMedicalCode = medicalCode;
            }
        }
        if (resultMedicalCode == null) {
            resultMedicalCode = new MedicalCodeVO();
        }

        return resultMedicalCode;
    }

    private EindexVO getParentChildHierarchy(Eindex eindex) {
        EindexVO resultEindexVO = null;
        List<Map<String, Object>> resultMap = eindexRepository.getParentChildList(eindex.getId());
        for (int x = 0; x < resultMap.size(); x++) {
            if (resultEindexVO == null) {
                resultEindexVO = populateEindexVO(resultMap.get(x));
            } else {
                EindexVO eindexVO = populateEindexVO(resultMap.get(x));
                eindexVO.setChild(resultEindexVO);
                resultEindexVO = eindexVO;
            }
        }
        if (resultEindexVO == null) {
            resultEindexVO = new EindexVO();
        }
        return resultEindexVO;
    }

    private EindexVO populateEindexVO(Map<String, Object> map) {
        EindexVO eindexVo = new EindexVO();
        eindexVo.setId(Integer.parseInt(map.get("id").toString()));
        eindexVo.setTitle(String.valueOf(map.get("title")));
        eindexVo.setSee(String.valueOf(map.get("see")));
        eindexVo.setSeealso(String.valueOf(map.get("seealso")));
        eindexVo.setIsmainterm(Boolean.valueOf(map.get("ismainterm").toString()));
        eindexVo.setCode(String.valueOf(map.get("code")));
        eindexVo.setNemod(String.valueOf(map.get("nemod")));
        eindexVo.calculateType();
        return eindexVo;
    }

    private EindexVO populateEindexVO(Map<String, Object> map, String code) {
        EindexVO eindexVo = populateEindexVO(map);
        if (code != null) {
            eindexVo.setCode(code);
        }
        return eindexVo;
    }

    private MedicalCodeVO populateMedicalCode(Map<String, Object> m) {
        MedicalCodeVO medicalCode = new MedicalCodeVO();
        medicalCode.setId(Integer.valueOf(String.valueOf(m.get("id"))));
        medicalCode.setTitle(String.valueOf(m.get("title")));
        medicalCode.setSee(String.valueOf(m.get("see")));
        medicalCode.setSeealso(String.valueOf(m.get("seealso")));
        medicalCode.setIsmainterm(Boolean.valueOf(String.valueOf(m.get("ismainterm"))));
        medicalCode.setNemod(String.valueOf(m.get("nemod")));
        medicalCode.setCode(Arrays.asList(String.valueOf(m.get("code")).split(",")));
        return medicalCode;
    }


}
