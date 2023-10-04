package com.emedlogix.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Column;


@Document(indexName = "codes")
public class CodeInfo {
    @Id
    @javax.persistence.Id
    @Column(name = "id", nullable = false)
    private String id;


    @Field(type = FieldType.Text, name = "code")
    private String code;
    @Field(type = FieldType.Text, name = "description")
    private String description;
    @Field(type = FieldType.Text,name = "version")
    private String version;
    private String type;

    public String getType() {
        if (code != null && description != null) {
            type = "code";
        } else {
            type = null;
        }
        return type;
    }

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
        this.id = code;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}


