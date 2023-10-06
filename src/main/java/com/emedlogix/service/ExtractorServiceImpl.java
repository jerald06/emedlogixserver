package com.emedlogix.service;

import static com.emedlogix.util.Constants.COMPLETED;
import static com.emedlogix.util.Constants.DRUG;
import static com.emedlogix.util.Constants.INDEX;
import static com.emedlogix.util.Constants.INPROGRESS;
import static com.emedlogix.util.Constants.NEOPLASM;
import static com.emedlogix.util.Constants.ORDER;
import static com.emedlogix.util.Constants.TABULAR;
import static com.emedlogix.util.Constants.ALTERNATE_TERMS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import com.emedlogix.codes.*;
import com.emedlogix.entity.*;
import com.emedlogix.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.emedlogix.index.ICD10CMIndex;
import com.emedlogix.index.MainTerm;
import com.emedlogix.index.Term;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

@Service
public class ExtractorServiceImpl implements ExtractorService {

    public static final Logger logger = LoggerFactory.getLogger(ExtractorServiceImpl.class);

    @Autowired
    ESCodeInfoRepository esCodeInfoRepository;

    @Autowired
    DBCodeDetailsRepository dbCodeDetailsRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    ChapterRepository chapterRepository;

    @Autowired
    EindexRepository eindexRepository;

    @Autowired
    NeoPlasmRepository neoPlasmRepository;

    @Autowired
    NeoPlasmCodeRepository neoPlasmCodeRepository;

    @Autowired
    DrugRepository drugRepository;

    @Autowired
    DrugCodeRepository drugCodeRepository;

    @Autowired
    TermHierarchyRepository hierarchyRepository;

    @Autowired
    NeoplasmHierarchyRepository neoplasmHierarchyRepository;

    @Autowired
    DrugHierarchyRepository drugHierarchyRepository;

    @Autowired
    FileStatusReposistory fileStatusReposistory;
    @Autowired
    ESAlterTermRepository esAlterTermRepository;

    private List<Section> parseSection(DiagnosisType diagnosisType, String version, String icdRef, String chapterId, List<Section> sections) throws JsonProcessingException {
        List<JAXBElement<?>> inclusionTermOrSevenChrNoteOrSevenChrDef = diagnosisType.getInclusionTermOrSevenChrNoteOrSevenChrDef();
        for (int i = 0; i < inclusionTermOrSevenChrNoteOrSevenChrDef.size(); i++) {
            if (inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getValue() instanceof NoteType) {
                String classification = inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getName().getLocalPart();
                NoteType noteTypeVaule = (NoteType) inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getValue();
                sections.add(parseItems(noteTypeVaule, version, icdRef, chapterId, diagnosisType.getName().getContent().get(0).toString(), classification));
            } else {
                if (inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getValue() instanceof DiagnosisType) {
                    parseSection((DiagnosisType) inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getValue(), version, icdRef, chapterId, sections);
                }
                if (inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getValue() instanceof VisualImpairmentType) {
                    VisualImpairmentType visualImpairmentType = (VisualImpairmentType) inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getValue();
                    VisualImpairment visualImpairment = new VisualImpairment();
                    visualImpairmentType.getVisCategory().stream().forEach(
                            visCategory -> {
                                if (visCategory.getHeading() != null) visualImpairment.setCategoryHeading(
                                        ((ElementNSImpl) visCategory.getHeading()).getFirstChild().getNodeValue());
                                Category category = new Category();
                                visualImpairment.getCategoriesList().add(category);
                                if (visCategory.getValue() != null)
                                    category.setValue(((ElementNSImpl) visCategory.getValue()).getFirstChild().getNodeValue());
                                if (visCategory.getHeading() != null)
                                    visualImpairment.setCategoryHeading(((ElementNSImpl) visCategory.getHeading()).getFirstChild().getNodeValue());
                                visCategory.getVisRange().stream().forEach(visRange -> populateVisRange(visualImpairment, category, visRange));

                            }
                    );
                    ObjectWriter ow = new ObjectMapper().writer();
                    String json = ow.writeValueAsString(visualImpairment);
                    logger.info("JSON : {}", json);
                    sections.get(sections.size() - 1).setVisImpair(json);
                }if (inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getDeclaredType() == SevenChrDefType.class) {
                    SevenChrDefType sevenChrDefType = (SevenChrDefType) inclusionTermOrSevenChrNoteOrSevenChrDef.get(i).getValue();
                    SevenChrDef sevenChrDef = parseSevenChrDefType(sevenChrDefType);
                    ObjectWriter ow = new ObjectMapper().writer();
                    String json = ow.writeValueAsString(sevenChrDef);
                    logger.info("JSON : {}",json);
                    sections.get(sections.size() - 1).setSevenChr(json);

                }
            }
        }
        return sections;
    }

    private SevenChrDef parseSevenChrDefType(SevenChrDefType sevenChrDefType) {
        SevenChrDef sevenChrDef = new SevenChrDef();
        List<Extension> extensionList = new ArrayList<>();
        String noteValue=null;
        for (ContentType contentType : sevenChrDefType.getExtensionOrNote()) {
            if (contentType instanceof ExtensionType) {
                // Handle ExtensionType
                ExtensionType extensionType = (ExtensionType) contentType;
                String extensionValue = extensionType.getContent().get(0).toString();
                String charValue = extensionType.getChar();
                Extension extension = new Extension();
                extension.setExtensionValue(extensionValue);
                extension.setCharValue(charValue);
                extensionList.add(extension);
            } else if (contentType instanceof NoteType) {
                NoteType noteType = (NoteType) contentType;
                List<ContentType> noteContents = noteType.getNote();
                if (!noteContents.isEmpty()){
                    noteValue = noteContents.get(0).getContent().get(0).toString();
                }
            }
        }

        // Set the extensionList in the SevenChrDef object
        sevenChrDef.setExtensionList(extensionList);
        sevenChrDef.setNote(noteValue);

        return sevenChrDef;
    }


    private void populateVisRange(VisualImpairment visualImpairment, Category category, VisualImpairmentType.VisCategory.VisRange visRange) {
        if (visRange.getHeading() != null)
            visualImpairment.setRangeHeading(((ElementNSImpl) visRange.getHeading()).getFirstChild().getNodeValue());
        if (visRange.getVisMin() != null && visRange.getVisMin().getHeading() != null)
            visualImpairment.setMinHeading(((ElementNSImpl) visRange.getVisMin().getHeading()).getFirstChild().getNodeValue());
        if (visRange.getVisMax() != null && visRange.getVisMax().getHeading() != null)
            visualImpairment.setMaxHeading(((ElementNSImpl) visRange.getVisMax().getHeading()).getFirstChild().getNodeValue());
        VisRange range = new VisRange();
        if (visRange.getVisMax() != null && visRange.getVisMax().getValue() != null)
            range.setMax(((ElementNSImpl) visRange.getVisMax().getValue()).getFirstChild().getNodeValue());
        if (visRange.getVisMin() != null && visRange.getVisMin().getValue() != null)
            range.setMin(((ElementNSImpl) visRange.getVisMin().getValue()).getFirstChild().getNodeValue());
        category.getVisRangeList().add(range);
    }

    private Section parseItems(NoteType noteTypeVaule, String version, String icdRef, String chapterId, String code, String classification) {
        Section section = new Section();
        section.setCode(replaceDot(code));
        section.setVersion(version);
        section.setChapterId(chapterId);
        section.setIcdReference(icdRef);
        List<ContentType> contentTypes = noteTypeVaule.getNote();
        if (contentTypes != null && !contentTypes.isEmpty()) {
            for (ContentType contentType : contentTypes) {
                Iterator iter = contentType.getContent().listIterator();
                while (iter.hasNext()) {
                    String note = (String) iter.next();
                    Notes notes = new Notes();
                    notes.setClassification(classification);
                    notes.setNotes(note);
                    notes.setType("SECTION");
                    notes.setVersion(version);
                    notes.setId(UUID.randomUUID().toString());
                    notes.setSection(section);
                    section.getNotes().add(notes);
                }
            }
        }
        return section;
    }

    @Override
    public void doExtractCapterSectionXML() {
        loadCapterSectionData(2023, "icd10cm_tabular_2023.xml");
    }

    @Override
    public void loadCapterSectionData(Integer year, String fileName) {
        logger.info("Start Extracting Chapter Section from XML file:{}", year + "/" + fileName);
        try {
            JAXBContext context = JAXBContext.newInstance(ICD10CMTabular.class);
            ICD10CMTabular tabular = (ICD10CMTabular) context.createUnmarshaller()
                    .unmarshal(new InputStreamReader(new ClassPathResource(year + "/" + fileName).getInputStream()));
            String version = tabular.getVersion().getContent().get(0).toString();
            if (isFileCompletedOrProgress(TABULAR, year, fileName, version)) {
                return;
            }
            FileStatus fieStatus = fileStatusReposistory.save(populateFileStatus(TABULAR, year, fileName, INPROGRESS, version));
            String icdtitle = tabular.getIntroduction().getIntroSection().get(0).getTitle().getContent().get(0).toString();
            icdtitle = icdtitle.substring(icdtitle.indexOf(" "));
            //icd_id: tabular.getIntroduction().getIntroSection().get(0).getTitle().getContent().get(0);
            Iterator<ChapterType> tabIter = tabular.getChapter().listIterator();
            while (tabIter.hasNext()) {
                ChapterType chapterType = tabIter.next();
                Chapter chapter = new Chapter();
                chapter.setIcdReference(icdtitle);
                chapter.setName(chapterType.getName().getContent().get(0).toString());
                chapter.setDescription(chapterType.getDesc().getContent().get(0).toString());
                chapter.setVersion(version);


                List<SectionReference> sectionReferences = new ArrayList<>();
                ListIterator<JAXBElement<?>> NodeIter = chapterType.getInclusionTermOrSevenChrNoteOrSevenChrDef().listIterator();
                while (NodeIter.hasNext()) {
                    JAXBElement element = NodeIter.next();
                    if (element.getValue() instanceof NoteType) {
                        NoteType noteType = (NoteType) element.getValue();
                        Iterator<ContentType> contenIter = noteType.getNote().listIterator();
                        while (contenIter.hasNext()) {
                            ContentType contentType = contenIter.next();
                            Iterator iter = contentType.getContent().listIterator();
                            while (iter.hasNext()) {
                                String note = (String) iter.next();
                                Notes notes = new Notes();
                                notes.setClassification(element.getName().toString());
                                notes.setNotes(note);
                                notes.setType("CHAPTER");
                                notes.setVersion(version);
                                notes.setId(UUID.randomUUID().toString());
                                notes.setChapter(chapter);
                                chapter.getNotes().add(notes);
                            }
                        }
                    } else if (element.getValue() instanceof SectionType) {
                        List<Section> sections = new LinkedList<>();
                        String sectionRefId = ((SectionType) element.getValue()).getId();
                        SectionType sectionType = (SectionType) element.getValue();
                        if (sectionType.getDeactivatedOrDiag() != null && !sectionType.getDeactivatedOrDiag().isEmpty()) {
                            for (Object diagnosisTypeObj : sectionType.getDeactivatedOrDiag()) {
                                DiagnosisType diagnosisType = (DiagnosisType) diagnosisTypeObj;
                                parseSection(diagnosisType, chapter.getVersion(), chapter.getIcdReference(), chapter.getName(), sections);
                            }
                            //save section;
                            logger.info("Saving Section into DB: size {}", sections.size());
                            sectionRepository.saveAll(sections);
                            logger.info("Saved Section into DB: Successfully {}", sections.size());
                        } else {
                            //parseSection(sectionType.getInclusionTermOrSevenChrNoteOrSevenChrDef().listIterator()
                            //      , chapter.getVersion(), chapter.getIcdReference());
                        }
                    } else if (element.getValue() instanceof SectionIndexType) {
                        SectionReference sectionReference = new SectionReference();
                        SectionIndexType sectionIndexType = (SectionIndexType) element.getValue();
                        sectionReference.setId(sectionIndexType.getId());
                        sectionReference.setIcdReference(chapter.getIcdReference());
                        sectionReference.setNotes(sectionIndexType.getSectionRef().get(0).getValue());
                        sectionReference.setLast(sectionIndexType.getSectionRef().get(0).getLast());
                        sectionReference.setFirst(sectionIndexType.getSectionRef().get(0).getFirst());
                        sectionReference.setVersion(chapter.getVersion());
                        sectionReference.setChapterId(chapter.getName());
                        sectionReferences.add(sectionReference);

                    }
                }
                //save chapter
                logger.info("Saving Chanpter :{}", chapter.getName());
                chapterRepository.save(chapter);
                fieStatus.setStatus(COMPLETED);
                fileStatusReposistory.save(fieStatus);
            }
        } catch (Exception e) {
            //TODO cathc the right exception and log it
            logger.error(e.toString(), e);
        }
        logger.info("Chapter section from XML successfully extracted:");
    }

    private boolean isFileCompletedOrProgress(String fileType, Integer year, String fileName, String version) {
        FileStatus fileStatus = fileStatusReposistory.findFileStatus(fileType, year, fileName, version);
        if (fileStatus != null && (fileStatus.getStatus().equalsIgnoreCase(INPROGRESS)
                || fileStatus.getStatus().equalsIgnoreCase(COMPLETED))) {
            return true;
        }
        return false;
    }

    @Override
    public void doExtractOrderedCodes() {
        loadOrderedCodesData(2023, "icd10cm_order_2023.txt");
    }

    @Override
    public void loadOrderedCodesData(Integer year, String fileName) {
        logger.info("Start Extracting Ordered Codes from file {}", fileName);
        Map<String, CodeDetails> codeDetailsMap = new HashMap<>();
        Map<String, CodeInfo> codeMap = new HashMap<>();
        FileStatus fieStatus = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(year + "/" + fileName).getInputStream()));
            if (isFileCompletedOrProgress(ORDER, year, fileName, year + "")) {
                return;
            }
            fieStatus = fileStatusReposistory.save(populateFileStatus(ORDER, year, fileName, INPROGRESS, year + ""));
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    CodeDetails details = parseCodeDetails(line);
                    details.setVersion(year.toString());
                    codeDetailsMap.put(details.getCode(), details);
                    CodeInfo codeInfo = new CodeInfo();
                    codeInfo.setCode(details.getCode());
                    codeInfo.setDescription(details.getLongDescription());
                    codeInfo.setVersion(String.valueOf(year));
                    codeMap.put(codeInfo.getCode(), codeInfo);
                }
            }
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }

        doSaveCodesToES(codeMap);
        doSaveOrderedCodesToDB(codeDetailsMap);
        if (fieStatus != null) {
            fieStatus.setStatus(COMPLETED);
            fileStatusReposistory.save(fieStatus);
        }
        logger.info("Code details successfully extracted ordered codes {}", codeDetailsMap.size());

    }

    @Override
    public void loadAlternateTermsData(Integer year, String fileName) {
        logger.info("Start Extracting Alternate Terms from file {}", fileName);
        FileStatus fieStatus = null;

        // Check if the file is already in progress or completed
        if (isFileCompletedOrProgress(ALTERNATE_TERMS, year, fileName, year + "")) {
            logger.info("File {} is already in progress or completed. Skipping extraction.", fileName);
            return;
        }

        ClassPathResource resource = new ClassPathResource(year + "/" + fileName);
        fieStatus = fileStatusReposistory.save(populateFileStatus(ALTERNATE_TERMS, year, fileName, INPROGRESS, year + ""));
        try (Workbook workbook = WorkbookFactory.create(resource.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell codeCell = row.getCell(0);
                Cell descriptionCell = row.getCell(1);
                if (codeCell != null && descriptionCell != null) {
                    String code = codeCell.getStringCellValue();
                    String description = descriptionCell.getStringCellValue();

                    try {
                        // Check if a record with the same code already exists in Elasticsearch
                        Optional<AlterTerm> existingTerm = esAlterTermRepository.findByCode(code);

                        if (existingTerm.isPresent()) {
                            // Duplicate found, create a new document with a unique ID
                            AlterTerm duplicateTerm = new AlterTerm();
                            duplicateTerm.setCode(code);
                            duplicateTerm.setAlterDescription(description);

                            duplicateTerm.setVersion(year.toString());
                            // Generate a unique ID for the duplicate record (e.g., using UUID)
                            String uniqueId = UUID.randomUUID().toString();
                            duplicateTerm.setId(uniqueId);

                            // Save the duplicate record with the unique ID
                            esAlterTermRepository.save(duplicateTerm);
                        } else {
                            // If no record with the same code exists, save the new record
                            AlterTerm alterTerm = new AlterTerm();
                            alterTerm.setCode(code);
                            alterTerm.setAlterDescription(description);
                            esAlterTermRepository.save(alterTerm);
                        }
                    } catch (Exception e) {
                        // Log any exceptions while continuing with the next record
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error extracting alternate terms from file", e);
        }

        if (fieStatus != null) {
            fieStatus.setStatus(COMPLETED);
            fileStatusReposistory.save(fieStatus);
        }

        logger.info("Alternate Terms successfully extracted.");
    }

    private void doSaveCodesToES(Map<String, CodeInfo> codeMap) {
        if (codeMap != null && !codeMap.isEmpty()) {
            logger.info("Total codes extracted {}", codeMap.entrySet().size());

            // Retrieve existing data from Elasticsearch
            Iterable<CodeInfo> existingCodeInfoList = esCodeInfoRepository.findAll();

            // Create a map to store existing code entries for quick lookup
            Map<String, CodeInfo> existingCodeMap = new HashMap<>();
            for (CodeInfo existingCodeInfo : existingCodeInfoList) {
                existingCodeMap.put(existingCodeInfo.getCode(), existingCodeInfo);
            }

            // Create a list to store the merged code entries (new + existing)
            List<CodeInfo> mergedCodeInfoList = new ArrayList<>();

            // Iterate through the codeMap and merge new data with existing data
            for (CodeInfo newCodeInfo : codeMap.values()) {
                String code = newCodeInfo.getCode();

                // Check if the code already exists in the existing data
                if (!existingCodeMap.containsKey(code)) {
                    // Code is not in the existing data, add it to the merged list
                    mergedCodeInfoList.add(newCodeInfo);
                } else {
                    // Code already exists, generate a new unique ID and add it
                    CodeInfo existingCode = existingCodeMap.get(code);
                    // Generate a new unique ID using UUID
                    String newUniqueId = UUID.randomUUID().toString();
                    newCodeInfo.setId(newUniqueId);
                    // Add the updated code to the merged list
                    mergedCodeInfoList.add(newCodeInfo);
                }
            }

            // Save the merged code info entries to Elasticsearch
            if (!mergedCodeInfoList.isEmpty()) {
                logger.info("Saving {} codes (including duplicates with new IDs) to Elasticsearch.", mergedCodeInfoList.size());
                esCodeInfoRepository.saveAll(mergedCodeInfoList);
            }
        }
        logger.info("Extractor Service Codes completed...");
    }



    private void doSaveOrderedCodesToDB(Map<String, CodeDetails> codeDetailsMap) {
        if (codeDetailsMap != null && !codeDetailsMap.isEmpty()) {
            logger.info("Total codes extracted {}", codeDetailsMap.entrySet().size());
            Iterator<Map.Entry<String, CodeDetails>> itr = codeDetailsMap.entrySet().iterator();
            ArrayList dataList = new ArrayList();
            int counter = 0;
            while (itr.hasNext()) {
                Map.Entry<String, CodeDetails> entry = itr.next();
                CodeDetails fetchCodeDetails = entry.getValue();
                fetchCodeDetails.setId(UUID.randomUUID().toString());
                dataList.add(fetchCodeDetails);
                counter++;
                if (dataList.size() % 2000 == 0) {
                    dbCodeDetailsRepository.saveAll(dataList);
                    dataList.clear();

                }
            }
            if (!dataList.isEmpty()) {
                dbCodeDetailsRepository.saveAll(dataList);
            }
        }
        logger.info("Extractor Service Ordered Codes completed...");
    }

    private CodeDetails parseCodeDetails(String input) {
        String[] tokens = input.split("[(?=\\s*$)]");
        CodeDetails codeDetails = new CodeDetails();
        int counter = 0;
        boolean skip = false;
        for (String token : tokens) {
            if (!token.isEmpty()) {
                if (counter == 3 && Character.isUpperCase(token.charAt(0)) && skip) {
                    if (token.length() > 2 && codeDetails.getShortDescription().startsWith(token.substring(0, 2))) {
                        counter++;
                    }
                }
                switch (counter) {
                    case 0:
                        counter++;
                        break;
                    case 1:
                        codeDetails.setCode(token);
                        counter++;
                        break;
                    case 2:
                        codeDetails.setBillable((token.equals("1")));
                        counter++;
                        break;
                    case 3:
                        codeDetails.setShortDescription((concateDescription(codeDetails.getShortDescription(), token)).trim());
                        skip = true;
                        //counter++;
                        break;
                    case 4:
                        codeDetails.setLongDescription((concateDescription(codeDetails.getLongDescription(), token)).trim());
                        //counter++;
                        break;
                }
            } else {
                if (codeDetails.getShortDescription() != null && codeDetails.getShortDescription().length() > 0 && counter == 3) {
                    counter++;
                }
            }
        }
        return codeDetails;
    }

    private String concateDescription(String previous, String current) {
        return (previous == null ? "" : previous) + " " + current.trim();
    }

    @Override
    public void loadNeoplasmData(Integer year, String fileName) {
        Object obj = parseXML(year + "/" + fileName, ICD10CMIndex.class);
        if (obj instanceof ICD10CMIndex) {
            ICD10CMIndex icd10CMIndex = (ICD10CMIndex) obj;
            String version = icd10CMIndex.getVersion();
            if (isFileCompletedOrProgress(NEOPLASM, year, fileName, version)) {
                return;
            }
            FileStatus fieStatus = fileStatusReposistory
                    .save(populateFileStatus(NEOPLASM, year, fileName, INPROGRESS, version));
            icd10CMIndex.getLetter().stream().forEach(l -> {
                l.getMainTerm().stream().forEach(m -> {
                    final Neoplasm neoplasmOne = populateNeoPlasmMainTerm(m, version);

                    // store neoplasm hierarchy
                    List<Integer> ids = new ArrayList<>();
                    ids.add(neoplasmOne.getId());
                    saveNeoplasmHierarchy(neoplasmOne.getId(), neoplasmOne.getId(), 0);

                    //store neoplasmcode
                    List<NeoPlasmCode> neoplasmCodes = new ArrayList<>();
                    m.getCell().stream().forEach(cell -> {
                        cell.getContent().stream().forEach(code -> {
                            populateNeoPlasmCode(neoplasmOne, neoplasmCodes, code);
                        });
                    });
                    neoPlasmCodeRepository.saveAll(neoplasmCodes);

                    if (!m.getTerm().isEmpty()) {
                        parseNeoPlasmLevelTerm(m.getTerm(), ids, version);
                    }
                });
            });
            fieStatus.setStatus(COMPLETED);
            fileStatusReposistory.save(fieStatus);
        }
    }

    private Neoplasm populateNeoPlasmMainTerm(MainTerm m, String version) {
        Neoplasm neoplasm = new Neoplasm();
        neoplasm.setTitle(m.getTitle().getContent().get(0).toString());
        if (m.getTitle().getContent().size() > 1) {
            neoplasm.setNemod(getNemodVal(m.getTitle().getContent().get(1)));
        }
        neoplasm.setSee(m.getSee());
        neoplasm.setSeealso(m.getSeeAlso());
        neoplasm.setIsmainterm(true);
        neoplasm.setVersion(version);
        return neoPlasmRepository.save(neoplasm);
    }

    public Object parseXML(String fileName, Class<?> className) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(className);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return jaxbUnmarshaller.unmarshal(new InputStreamReader(new ClassPathResource(fileName).getInputStream()));
        } catch (JAXBException | IOException e) {
            logger.error("doExtractICD10CMCodes error...", e.getMessage(), e.fillInStackTrace());
        }
        return new Object();
    }

    private void parseNeoPlasmLevelTerm(List<Term> termType, List<Integer> ids, String version) {
        termType.forEach(a -> {
            final Neoplasm neoplasm = populateNeoPlasmLavelTerm(a, version);

            //store hierarchy
            if (a.getLevel() == ids.size()) {
                ids.add(neoplasm.getId());
            } else {
                for (int i = ids.size() - 1; i >= a.getLevel(); i--) {
                    ids.remove(i);
                }
                ids.add(neoplasm.getId());
            }
            int level = 0;
            for (int i = ids.size() - 1; i >= 0; i--) {
                saveNeoplasmHierarchy(ids.get(i), neoplasm.getId(), level);
                level++;
            }

            //store neoplasmcode
            List<NeoPlasmCode> neoplasmCodes = new ArrayList<>();
            a.getCell().forEach(c -> {
                c.getContent().stream().forEach(j -> {
                    populateNeoPlasmCode(neoplasm, neoplasmCodes, j);
                });
            });
            neoPlasmCodeRepository.saveAll(neoplasmCodes);

            if (!a.getTerm().isEmpty()) {
                parseNeoPlasmLevelTerm(a.getTerm(), ids, version);
            }
        });
    }

    private void populateNeoPlasmCode(final Neoplasm neoplasmOne, List<NeoPlasmCode> neoplasmCodes, Object obj) {
        NeoPlasmCode neoPlasmCode = new NeoPlasmCode();
        neoPlasmCode.setNeoplasm_id(neoplasmOne.getId());
        neoPlasmCode.setCode(replaceDot(obj.toString()));
        neoplasmCodes.add(neoPlasmCode);
    }

    private Neoplasm populateNeoPlasmLavelTerm(Term a, String version) {
        Neoplasm neoplasm = new Neoplasm();
        neoplasm.setTitle(a.getTitle().getContent().get(0).toString());
        if (a.getTitle().getContent().size() > 1) {
            neoplasm.setNemod(getNemodVal(a.getTitle().getContent().get(1)));
        }
        neoplasm.setSee(a.getSee());
        neoplasm.setSeealso(a.getSeeAlso());
        neoplasm.setIsmainterm(false);
        neoplasm.setVersion(version);
        return neoPlasmRepository.save(neoplasm);
    }

    @Override
    public void doExtractIndex() {//test_index.xml, icd10cm_index_2023.xml,icd10cm_eindex_2023.xml
        loadIndexData(2023, "icd10cm_index_2023.xml");
        loadIndexData(2023, "icd10cm_eindex_2023.xml");

    }

    @Override
    public void loadIndexData(Integer year, String fileName) {
        Object obj = parseXML(year + "/" + fileName, ICD10CMIndex.class);
        if (obj instanceof ICD10CMIndex) {
            ICD10CMIndex icd10CMIndex = (ICD10CMIndex) obj;
            String version = icd10CMIndex.getVersion();
            if (isFileCompletedOrProgress(INDEX, year, fileName, version)) {
                return;
            }
            FileStatus fieStatus = fileStatusReposistory
                    .save(populateFileStatus(INDEX, year, fileName, INPROGRESS, version));
            icd10CMIndex.getLetter().stream().forEach(l -> {
                l.getMainTerm().stream().forEach(m -> {
                    Eindex index = populateAndSaveEIndex(m, version);
                    List<Integer> ids = new ArrayList<>();
                    ids.add(index.getId());
                    populateAndSaveHierarchy(index.getId(), index.getId(), 0);
                    if (!m.getTerm().isEmpty()) {
                        parseEIndexLevelTerm(m.getTerm(), ids, version);
                    }
                    EindexVO eindexVO = new EindexVO();
                    eindexVO.setVersion(year.toString());
                });
            });
            fieStatus.setStatus(COMPLETED);
            fileStatusReposistory.save(fieStatus);
        }
    }

    private Eindex populateAndSaveEIndex(MainTerm m, String version) {
        Eindex eIndex = new Eindex();
        eIndex.setTitle(m.getTitle().getContent().get(0).toString());
        if (m.getTitle().getContent().size() > 1) {
            eIndex.setNemod(getNemodVal(m.getTitle().getContent().get(1)));
        }
        eIndex.setCode(replaceDot(m.getCode()));
        eIndex.setSee(m.getSee());
        eIndex.setSeealso(m.getSeeAlso());
        eIndex.setSeecat(m.getSeecat());
        eIndex.setIsmainterm(true);
        eIndex.setVersion(version);
        return eindexRepository.save(eIndex);
    }

    private void populateAndSaveHierarchy(Integer parentId, Integer childId, Integer level) {
        TermHierarchy termHierarchy = new TermHierarchy();
        termHierarchy.setParentId(parentId);
        termHierarchy.setChildId(childId);
        termHierarchy.setLevel(level);
        hierarchyRepository.save(termHierarchy);
    }

    private void parseEIndexLevelTerm(List<Term> term, List<Integer> ids, String version) {
        term.forEach(a -> {
            Eindex index = populateAndSaveEIndexLevelTerm(a, version);
            if (a.getLevel() == ids.size()) {
                ids.add(index.getId());
            } else {
                for (int i = ids.size() - 1; i >= a.getLevel(); i--) {
                    ids.remove(i);
                }
                ids.add(index.getId());
            }
            int level = 0;
            for (int i = ids.size() - 1; i >= 0; i--) {
                populateAndSaveHierarchy(ids.get(i), index.getId(), level);
                level++;
            }
            if (!a.getTerm().isEmpty()) {
                parseEIndexLevelTerm(a.getTerm(), ids, version);
            }

        });
    }

    private Eindex populateAndSaveEIndexLevelTerm(Term m, String version) {
        Eindex eIndex = new Eindex();
        eIndex.setTitle(m.getTitle().getContent().get(0).toString());
        if (m.getTitle().getContent().size() > 1) {
            eIndex.setNemod(getNemodVal(m.getTitle().getContent().get(1)));
        }
        eIndex.setCode(replaceDot(m.getCode()));
        eIndex.setSee(m.getSee());
        eIndex.setSeealso(m.getSeeAlso());
        eIndex.setSeecat(m.getSeecat());
        eIndex.setIsmainterm(false);
        eIndex.setVersion(version);
        return eindexRepository.save(eIndex);
    }

    @Override
    public void loadDrugData(Integer year, String fileName) {//icd10cm_drug_2023.xml, test_drug.xml
        Object obj = parseXML(year + "/" + fileName, ICD10CMIndex.class);
        if (obj instanceof ICD10CMIndex) {
            ICD10CMIndex icd10CMIndex = (ICD10CMIndex) obj;
            String version = icd10CMIndex.getVersion();
            if (isFileCompletedOrProgress(DRUG, year, fileName, version)) {
                return;
            }
            FileStatus fieStatus = fileStatusReposistory
                    .save(populateFileStatus(DRUG, year, fileName, INPROGRESS, version));
            icd10CMIndex.getLetter().stream().forEach(l -> {
                l.getMainTerm().stream().forEach(m -> {
                    final Drug drug = populateDrugMainTerm(m, version);

                    //save drug hierarchy
                    List<Integer> ids = new ArrayList<>();
                    ids.add(drug.getId());
                    saveDrugHierarchy(drug.getId(), drug.getId(), 0);

                    List<DrugCode> drugCodes = new ArrayList<>();
                    m.getCell().stream().forEach(cell -> {
                        cell.getContent().stream().forEach(code -> {
                            populateDrugCode(drug, drugCodes, code);
                        });
                    });
                    drugCodeRepository.saveAll(drugCodes);

                    if (!m.getTerm().isEmpty()) {
                        parseDrugLevelTerm(m.getTerm(), ids, version);
                    }
                });
            });
            fieStatus.setStatus(COMPLETED);
            fileStatusReposistory.save(fieStatus);
        }
    }

    private Drug populateDrugMainTerm(MainTerm m, String version) {
        Drug drug = new Drug();
        drug.setTitle(m.getTitle().getContent().get(0).toString());
        if (m.getTitle().getContent().size() > 1) {
            drug.setNemod(getNemodVal(m.getTitle().getContent().get(1)));
        }
        drug.setSee(m.getSee());
        drug.setSeealso(m.getSeeAlso());
        drug.setIsmainterm(true);
        drug.setVersion(version);
        return drugRepository.save(drug);
    }

    private void populateDrugCode(final Drug drug, List<DrugCode> drugCodes, Object code) {
        DrugCode drugCode = new DrugCode();
        drugCode.setDrug_id(drug.getId());
        drugCode.setCode(replaceDot(code.toString()));
        drugCodes.add(drugCode);
    }

    private void parseDrugLevelTerm(List<Term> termType, List<Integer> ids, String version) {
        termType.forEach(a -> {
            final Drug drug = populateDrugLevelTerm(a, version);

            //store drug hierarchy
            if (a.getLevel() == ids.size()) {
                ids.add(drug.getId());
            } else {
                for (int i = ids.size() - 1; i >= a.getLevel(); i--) {
                    ids.remove(i);
                }
                ids.add(drug.getId());
            }
            int level = 0;
            for (int i = ids.size() - 1; i >= 0; i--) {
                saveDrugHierarchy(ids.get(i), drug.getId(), level);
                level++;
            }

            //save drug codes
            List<DrugCode> drugCodes = new ArrayList<>();
            a.getCell().forEach(c -> {
                c.getContent().stream().forEach(j -> {
                    populateDrugCode(drug, drugCodes, j);
                });
            });
            drugCodeRepository.saveAll(drugCodes);

            if (!a.getTerm().isEmpty()) {
                parseDrugLevelTerm(a.getTerm(), ids, version);
            }
        });
    }

    private Drug populateDrugLevelTerm(Term a, String version) {
        Drug drug = new Drug();
        drug.setTitle(a.getTitle().getContent().get(0).toString());
        if (a.getTitle().getContent().size() > 1) {
            drug.setNemod(getNemodVal(a.getTitle().getContent().get(1)));
        }
        drug.setSee(a.getSee());
        drug.setSeealso(a.getSeeAlso());
        drug.setIsmainterm(false);
        drug.setVersion(version);
        return drugRepository.save(drug);
    }

    private String replaceDot(String input) {
        if (input != null) {
            input = input.replace(".", "");
        }
        return input;
    }

    private String getNemodVal(Object obj) {
        if (obj instanceof JAXBElement) {
            JAXBElement element = (JAXBElement) obj;
            return element.getValue().toString();
        }
        return new String();
    }

    private void saveDrugHierarchy(Integer parentId, Integer childId, Integer level) {
        DrugHierarchy drugHierarchy = new DrugHierarchy();
        drugHierarchy.setParentId(parentId);
        drugHierarchy.setChildId(childId);
        drugHierarchy.setLevel(level);
        drugHierarchyRepository.save(drugHierarchy);
    }

    private void saveNeoplasmHierarchy(Integer parentId, Integer childId, Integer level) {
        NeoplasmHierarchy neoplasmHierarchy = new NeoplasmHierarchy();
        neoplasmHierarchy.setParentId(parentId);
        neoplasmHierarchy.setChildId(childId);
        neoplasmHierarchy.setLevel(level);
        neoplasmHierarchyRepository.save(neoplasmHierarchy);
    }

    @Override
    public void doExtractNeoplasm() {
        loadNeoplasmData(2023, "icd10cm_neoplasm_2023.xml");
    }

    @Override
    public void doExtractDrug() {
        loadDrugData(2023, "icd10cm_drug_2023.xml");
    }

    @Override
    public void doExtractAlternateTermXLSX() {
        loadAlternateTermsData(2023, "Alternate-Terms-2023.xlsx");
    }

    private FileStatus populateFileStatus(String fileType, Integer year, String fileName, String status, String version) {
        FileStatus fileStatus = new FileStatus();
        fileStatus.setFileType(fileType);
        fileStatus.setYear(year);
        fileStatus.setFileName(fileName);
        fileStatus.setStatus(status);
        fileStatus.setVersion(version);
        return fileStatus;
    }
}


