package com.emedlogix.entity;

import org.aspectj.weaver.ast.Not;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "emed_chapter")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String icdReference;
    String description;
    String name;
    @OneToMany(mappedBy="chapter")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    List<Notes> notes;
    String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcdReference() {
        return icdReference;
    }

    public void setIcdReference(String icdReference) {
        this.icdReference = icdReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Notes> getNotes() {
        notes = notes==null?new ArrayList<>():notes;
        return notes;
    }

    public void setNotes(List<Notes> notes) {
        this.notes = notes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "id='" + id + '\'' +
                ", icdReference='" + icdReference + '\'' +
                ", description='" + description + '\'' +
                ", notes=" + notes +
                ", version='" + version + '\'' +
                '}';
    }
}
