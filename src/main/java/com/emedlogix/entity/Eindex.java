package com.emedlogix.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "eindex")
@Table(name="eindex")
public class Eindex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    
    String title;
    String code;
    String see;
    String seealso;
    String seecat;
    String nemod;
    String version;
    Boolean ismainterm;
}
