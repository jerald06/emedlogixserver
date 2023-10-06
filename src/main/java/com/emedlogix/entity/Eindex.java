package com.emedlogix.entity;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
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
    @Transient
    private String type;

    @PostLoad
    private void calculateType() {
        if (code != null && see == null && seealso == null && seecat == null && !ismainterm) {
            type = "code";
        } else if (code == null && see != null && seealso == null && seecat == null && !ismainterm) {
            type = "see";
        } else if (code == null && see == null && seealso != null && seecat == null && !ismainterm) {
            type = "seealso";
        } else if (code == null && see == null && seealso == null && seecat != null && !ismainterm) {
            type = "seecat";
        }
        else if (code != null && seealso != null) {
            type = "seealso";
        } else if (code != null && see != null) {
            type = "see";
        } else if (code != null && seecat != null) {
            type = " seecat";
        } else if (code != null && ismainterm){
            type = "ismainterm";
        } else if (see != null && ismainterm){
            type = "ismainterm";
        } else if (seealso != null && ismainterm){
            type = "ismainterm";
        }
    }

    public String getType() {
        return type;
    }


}
