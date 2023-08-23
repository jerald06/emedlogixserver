package com.emedlogix.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.emedlogix.repository.EindexRepository;
import com.emedlogix.repository.NeoPlasmRepository;
import com.emedlogix.repository.SectionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class CodeSearchService implements CodeSearchController {

    public static final Logger logger = LoggerFactory.getLogger(CodeSearchService.class);
    private static final String INDEX_NAME = "details";
    private static final String FIELD_NAME = "code";
    private Map<String,Object> indexMap = null;
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

    @Override
    public CodeInfo getCodeInfo(String code) {
        logger.info("Getting Code Information for:", code);
        CodeInfo codeInfo = esCodeInfoRepository.getByCode(code);
        return codeInfo;
    }

    public List<CodeInfo> getCodeInfoMatches(String code) {
        logger.info("Getting Code Information for code starts with:", code);
        List<CodeInfo> codeInfoList = new ArrayList<>();
        Iterable<CodeInfo> codeDetailsIterable = esCodeInfoRepository.findByCodeStartingWith(code);
        Iterator<CodeInfo> it = codeDetailsIterable.iterator();
        while (it.hasNext()) {
            CodeInfo codeInfo = it.next();
            codeInfoList.add(codeInfo);
        }
        logger.info("Got matching codes size:", codeInfoList.size());
        return codeInfoList;
    }

    public CodeDetails getCodeInfoDetails(@PathVariable String code){
        logger.info("Getting Code Information Details for code:", code);
        CodeDetails codeDetails = dbCodeDetailsRepository.findByCode(code);
        Section section = sectionRepository.findByCode(code);
        if(section != null) {
            codeDetails.setSection(section);
            chapterRepository.findById(section.getChapterId()).ifPresent(value -> {
                codeDetails.setChapter(value);
            });
        }
        //codeDetails.setChapter(.get());
        return codeDetails;
    }

	@Override
	public List<EindexVO> getEIndex(String code) {
		return eindexRepository.findMainTermBySearch(code).stream().map(m -> {
			return getParentChildHierarchy(m);
		}).collect(Collectors.toList());
	}

	@Override
	public List<MedicalCodeVO> getNeoPlasm(String code) {
		return neoPlasmRepository.findNeoplasmByCode(code).stream().map(m -> {
			return getDrugNeoplasmHierarchy(m,"neoplasm");
		}).collect(Collectors.toList());
	}
	
	@Override
	public List<MedicalCodeVO> getDrug(String code) {
		return drugRepository.findDrugByCode(code).stream().map(m -> {
			return getDrugNeoplasmHierarchy(m,"drug");
		}).collect(Collectors.toList());
	}

	@Override
	public List<EindexVO> getEIndexByNameSearch(String name,boolean mainTermSearch) {
		String[] names = name.trim().split(" ");
		if(names.length>1 && names.length == 2) {
			return multipleSearch(names, mainTermSearch);
		} else {
			if(mainTermSearch) {
				return singleMainTermSearch(names[0]);
			}
			else {
				return singleLevelTermSearch(names[0]);
			}
		}
	}

	private List<EindexVO> multipleSearch(String[] names, boolean mainTermSearch) {
		List<EindexVO> result = new ArrayList<>();
		if(mainTermSearch) {
			List<Eindex> mainTermResult = eindexRepository.findMainTerm(names[0]);
			if(mainTermResult.size()==0) {
				return result;
			}
			List<String> mainTermSeeSeeAlso = new ArrayList<>();
			mainTermResult.forEach( e -> {
				getMainTermSeeAndSeealso(e,mainTermSeeSeeAlso);
			});
			//mainTerm mainTerm(See/See Also term of main term has 2nd main term)
			if(mainTermSeeSeeAlso.contains(names[1])) {
				return mainTermResult.stream().map(i -> {
					return extractEintexVO(i);
				}).collect(Collectors.toList());
			}
			//mainTerm LevelTermof1stTerm
			Integer resultCount = eindexRepository.mainTermHasLevelTerm(names[0],names[1]);
			if(resultCount>0) {
				result = singleMainTermSearch(names[0]);
			}
			if(result.size()>0) {
				return result;
			}
			//mainTerm NotLevelTermof1stTerm(Show if See/See Also term of main term have the level term entered)
			result = eindexRepository.findSecondMainTermLevel(mainTermSeeSeeAlso,names[1]).stream().map(i -> {
				return extractEintexVO(i);
			}).collect(Collectors.toList());
			if(result.size()>0) {
				return result;
			}

			//mainTerm mainTerm(Else show all Level terms applicable for 2nd main term)
			result = singleMainTermSearch(names[1]);
			if(result.size()>0) {
				return result;
			}

			//mainTerm NotLevelTermof1stTerm(Else show all main terms associated with the level term entered)
			result = singleLevelTermSearch(names[1]);
			if(result.size()>0) {
				return result;
			}
			//mainTerm NotinIndextable(Else show all Level(1st level) terms applicable for 1st main term)
			result = singleMainTermSearch(names[0]);
			if(result.size()>0) {
				return result;
			}
			
		} else {
			//LevelTerm Maintermof1stterm
			Integer resultCount = eindexRepository.mainTermHasLevelTerm(names[1],names[0]);
			if(resultCount>0) {
				result = singleMainTermSearch(names[1]);
			}
			if(result.size()>0) {
				return result;
			}
			//LevelTerm NotinIndextable
			result = singleLevelTermSearch(names[0]);
			if(result.size()>0) {
				return result;
			}
		}
		return result;
	}

	private EindexVO extractEintexVO(Eindex i) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.convertValue(i, new TypeReference<Map<String, Object>>() {});
		return populateEindexVO(map);
	}

	private void getMainTermSeeAndSeealso(Eindex eindex,List<String> mainTermsTitle){
		if(eindex.getSee()!=null) {
			mainTermsTitle.addAll(Arrays.asList(eindex.getSee().split(",")));
		}
		if(eindex.getSeealso()!=null) {
			mainTermsTitle.addAll(Arrays.asList(eindex.getSeealso().split(",")));
		}
	}

	private List<EindexVO> singleLevelTermSearch(String name) {
		List<EindexVO> indexList = new ArrayList<>();
		eindexRepository.searchLevelTermMainTerm(name).forEach(map -> {
			if(indexMap!=null && Integer.parseInt(indexMap.get("childId").toString())!=Integer.parseInt(map.get("childId").toString())) {
				indexList.add(populateEindexVO(indexMap,code));
				code = null;
			}
			indexMap = map;
			if(map.get("code")!=null) {
				code = map.get("code").toString();
			}
		});
		indexList.add(populateEindexVO(indexMap,code));
		indexList.sort(Comparator.comparing(m -> m.getTitle(),
				Comparator.nullsLast(Comparator.naturalOrder())
		));
		return indexList;
	}

	private List<EindexVO> singleMainTermSearch(String name) {
		return eindexRepository.searchMainTermLevelOne(name).stream().map(m -> {
			return populateEindexVO(m);
		}).collect(Collectors.toList());
	}

	private MedicalCodeVO getDrugNeoplasmHierarchy(Map<String, Object> m,String type) {		
		MedicalCodeVO resultMedicalCode = null;
		List<Map<String,Object>> resultMap = new ArrayList<>();
		if (type == "neoplasm") {
			resultMap = neoPlasmRepository.getParentChildList(Integer.valueOf(String.valueOf(m.get("id"))));
		} else if (type == "drug") {
			resultMap = drugRepository.getParentChildList(Integer.valueOf(String.valueOf(m.get("id"))));
		}
		for(int x = 0; x < resultMap.size(); x++) {
			if(resultMedicalCode == null) {
				resultMedicalCode = populateMedicalCode(m);
			} else {
				MedicalCodeVO medicalCode = populateMedicalCode(resultMap.get(x));
				medicalCode.setChild(resultMedicalCode);
				resultMedicalCode = medicalCode;
			}
		}
		if(resultMedicalCode == null) {
			resultMedicalCode = new MedicalCodeVO();
		}
		
		return resultMedicalCode;
	}

	private EindexVO getParentChildHierarchy(Eindex eindex) {
		EindexVO resultEindexVO = null;
		List<Map<String,Object>> resultMap = eindexRepository.getParentChildList(eindex.getId());
		for(int x = 0; x < resultMap.size(); x++) {
			if(resultEindexVO == null) {
				resultEindexVO = populateEindexVO(resultMap.get(x));
			} else {
				EindexVO eindexVO = populateEindexVO(resultMap.get(x));
				eindexVO.setChild(resultEindexVO);
				resultEindexVO = eindexVO;
			}
		}
		if(resultEindexVO == null) {
			resultEindexVO = new EindexVO();
		}
		return resultEindexVO;
	}

	private EindexVO populateEindexVO(Map<String,Object> map) {
		EindexVO eindexVo = new EindexVO();
		eindexVo.setId(Integer.parseInt(map.get("id").toString()));
		eindexVo.setTitle(String.valueOf(map.get("title")));
		eindexVo.setSee(String.valueOf(map.get("see")));
		eindexVo.setSeealso(String.valueOf(map.get("seealso")));
		eindexVo.setIsmainterm(Boolean.valueOf(map.get("ismainterm").toString()));
		eindexVo.setCode(String.valueOf(map.get("code")));
		eindexVo.setNemod(String.valueOf(map.get("nemod")));
		return eindexVo;
	}
	
	private EindexVO populateEindexVO(Map<String,Object> map,String code) {
		EindexVO eindexVo = populateEindexVO(map);
		if(code!=null) {
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
