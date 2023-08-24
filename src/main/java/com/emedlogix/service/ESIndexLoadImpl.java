
package com.emedlogix.service;

import com.emedlogix.entity.Eindex;
import com.emedlogix.entity.EindexLevels;
import com.emedlogix.repository.ESIndexLevelSearchRepository;
import com.emedlogix.repository.EindexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ESIndexLoadImpl implements ESIndexLoad {

    @Autowired
    EindexRepository eindexRepository;

    @Autowired
    ESIndexLevelSearchRepository esIndexLevelSearchRepository;
    @Override
    public void  loadIndexMainTerm() {
       List<Eindex> eindexList = eindexRepository.findAll();
       List<EindexLevels> eindexLevels = new ArrayList<>();

       for (Eindex eindex : eindexList){
           EindexLevels eindexVO = getParentChildHierarchy(eindex.getId(),eindex.getTitle());
           eindexLevels.add(eindexVO);
           if (eindexLevels.size() >= 10000){
               esIndexLevelSearchRepository.saveAll(eindexLevels);
               eindexLevels.clear();
           }

       }

    }

    @Override
    public void loadIndexLevelTerm() {


    }

    private EindexLevels getParentChildHierarchy(Integer id, String title) {
        EindexLevels resultEindexVO = new EindexLevels();
        resultEindexVO.setTitle(title);
        resultEindexVO.setLevelTerms(getTerms(id,true));
        resultEindexVO.setMainTerms(getTerms(id,false));
        return resultEindexVO;
    }
    private EindexLevels.Eindex populateEindexVO(Map<String,Object> map) {
        EindexLevels.Eindex eindexVo = EindexLevels.getEindexInstance();
        eindexVo.setTitle(String.valueOf(map.get("title")));
        eindexVo.setSee(String.valueOf(map.get("see")));
        eindexVo.setSeealso(String.valueOf(map.get("seealso")));
        eindexVo.setIsmainterm(Boolean.valueOf(map.get("ismainterm").toString()));
        eindexVo.setCode(String.valueOf(map.get("code")));
        eindexVo.setNemod(String.valueOf(map.get("nemod")));
        return eindexVo;
    }

    private List<EindexLevels.Eindex> getTerms(Integer id, Boolean isLevel){
        List<Map<String,Object>> resultMap = isLevel? eindexRepository.getIndexLevelTerms(id,false)
                :eindexRepository.getIndexMainTerms(id,true);
        List<EindexLevels.Eindex> list = new ArrayList<>();

        for(int x = 0; x < resultMap.size(); x++) {

            EindexLevels.Eindex eindexVO = populateEindexVO(resultMap.get(x));
            list.add(eindexVO);
        }

        return list;
    }

}

