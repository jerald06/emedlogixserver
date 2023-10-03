package com.emedlogix.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EindexVO {

    private Integer id;    
    private String title;
    private String code;
    private String see;
    private String seealso;
    private Boolean ismainterm;
    private String nemod;
    private EindexVO child;
    private String derivedCode;
    private String type;

    public void calculateType() {
        if (ismainterm && code != null && !code.equalsIgnoreCase("null") && derivedCode != null) {
            if (see != null || (seealso != null && !seealso.equalsIgnoreCase("null"))) {
                type = "ismainterm";
            }
        } else if (ismainterm) {
            type = "ismainterm";
        } else {
            if (code == null || code.equalsIgnoreCase("null")) {
                if (see != null && (seealso == null || seealso.equalsIgnoreCase("null"))) {
                    type = "see";
                } else if (see != null && seealso != null) {
                    type = "seealso";
                }
            } else {
                type = "code";
            }
        }
    }
}
