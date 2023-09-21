package com.emedlogix.entity;

import java.util.ArrayList;
import java.util.List;

public class SevenChrDef {

    String note;
    List<Extension> extensionList;


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }



    public List<Extension> getExtensionList() {
        extensionList = extensionList == null ? new ArrayList<>() : extensionList;
        return extensionList;
    }

    public void setExtensionList(List<Extension> extensionList) {
        this.extensionList = extensionList;
    }

    @Override
    public String toString() {
        return "SevenChrDef{" +
                "note='" + note + '\'' +
                ", extensionList=" + extensionList +
                '}';
    }
}