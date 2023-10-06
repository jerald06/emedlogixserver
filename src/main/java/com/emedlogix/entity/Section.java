package com.emedlogix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "emed_section")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String code;
    String chapterId;
    String icdReference;
    String version;

    @OneToMany(mappedBy = "section")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    List<Notes> notes;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    VisualImpairment visualImpairment;

    @JsonIgnore
    String visImpair;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    SevenChrDef sevenChrDef;


    @JsonIgnore
    String sevenChr;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getIcdReference() {
        return icdReference;
    }

    public void setIcdReference(String icdReference) {
        this.icdReference = icdReference;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Notes> getNotes() {
        notes = notes == null ? new ArrayList<>() : notes;
        return notes;
    }

    public void setNotes(List<Notes> notes) {
        this.notes = notes;
    }

    public VisualImpairment getVisualImpairment() throws JsonProcessingException {
        visualImpairment = this.visImpair != null ? new ObjectMapper().readValue(this.visImpair, VisualImpairment.class) : null;
        return visualImpairment;
    }

    public void setVisualImpairment(VisualImpairment visualImpairment) throws JsonProcessingException {
        //visualImpairment = new ObjectMapper().readValue(this.visImpair, VisualImpairment.class);
        this.visualImpairment = visualImpairment;
    }

    public String getVisImpair() throws JsonProcessingException {
        return this.visImpair;
    }

    public void setVisImpair(String visImpair) {
        this.visImpair = visImpair;
    }

    public SevenChrDef getSevenChrDef() throws JsonProcessingException{
        sevenChrDef = this.sevenChr != null ? new ObjectMapper().readValue(this.sevenChr,SevenChrDef.class):null;
        return sevenChrDef;
    }

    public void setSevenChrDef(SevenChrDef sevenChrDef) {
        this.sevenChrDef = sevenChrDef;
    }

    public String getSevenChr() {
        return sevenChr;
    }

    public void setSevenChr(String sevenChr) {
        this.sevenChr = sevenChr;
    }

    @Override
    public String toString() {
        return "Section{" +
                "code='" + code + '\'' +
                ", chapterId='" + chapterId + '\'' +
                ", icdReference='" + icdReference + '\'' +
                ", version='" + version + '\'' +
                ", notes=" + notes +
                ", visualImpairment=" + visualImpairment +
                ", sevenChrDef=" + sevenChrDef +
                '}';
    }
}