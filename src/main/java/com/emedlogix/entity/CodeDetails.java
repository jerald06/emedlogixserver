package com.emedlogix.entity;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="emed_medical_codes")
public class CodeDetails{

    Boolean billable;
    String longDescription;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    private String code;

    private String shortDescription;
    @Column(name = "version",nullable = false)
    private String version;
    @Transient
    private Chapter chapter;
    @Transient
    private Section section;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Boolean getBillable() {
        return billable;
    }

    public void setBillable(Boolean billable) {
        this.billable = billable;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "CodeDetails{" +
                "billable=" + billable +
                ", longDescription='" + longDescription + '\'' +
                ", id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", version='" + version + '\'' +
                ", chapter=" + chapter +
                ", section=" + section +
                '}';
    }
}
