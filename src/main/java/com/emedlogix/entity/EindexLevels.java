
package com.emedlogix.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Column;

import java.util.List;

@Document(indexName = "terms")
public class EindexLevels {

    @Id
    @javax.persistence.Id
    @Column(name = "id", nullable = false)
    private String id;

    @Field(type = FieldType.Text, name = "title")
    String title;
    @Field(type = FieldType.Nested, name = "levelTerms")
    List<Eindex> levelTerms;
    @Field(type = FieldType.Nested, name = "mainTerms")
    List<Eindex> mainTerms;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Eindex> getLevelTerms() {
        return levelTerms;
    }

    public void setLevelTerms(List<Eindex> levelTerms) {
        this.levelTerms = levelTerms;
    }

    public List<Eindex> getMainTerms() {
        return mainTerms;
    }

    public void setMainTerms(List<Eindex> mainTerms) {
        this.mainTerms = mainTerms;
    }

    public static Eindex getEindexInstance(){
        return new Eindex();
    }

    public static class Eindex {
        String title;
        String level;
        String see;
        String seealso;
        String nemod;
        String code;
        Boolean ismainterm;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getSee() {
            return see;
        }

        public void setSee(String see) {
            this.see = see;
        }

        public String getSeealso() {
            return seealso;
        }

        public void setSeealso(String seealso) {
            this.seealso = seealso;
        }

        public String getNemod() {
            return nemod;
        }

        public void setNemod(String nemod) {
            this.nemod = nemod;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Boolean getIsmainterm() {
            return ismainterm;
        }

        public void setIsmainterm(Boolean ismainterm) {
            this.ismainterm = ismainterm;
        }
    }
}

