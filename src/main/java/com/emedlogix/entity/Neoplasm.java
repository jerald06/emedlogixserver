package com.emedlogix.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "neoplasm")
public class Neoplasm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    
    String title;
    String see;
    String seealso;
    String nemod;
    String version;
    Boolean ismainterm;
}
