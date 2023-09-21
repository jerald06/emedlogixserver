package com.emedlogix.entity;

public class Extension {
    String extensionValue;
    String charValue;


    public String getExtensionValue() {
        return extensionValue;
    }

    public void setExtensionValue(String extensionValue) {
        this.extensionValue = extensionValue;
    }

    public String getCharValue() {
        return charValue;
    }

    public void setCharValue(String charValue) {
        this.charValue = charValue;
    }

    @Override
    public String toString() {
        return "Extension{" +
                "extensionValue='" + extensionValue + '\'' +
                ", charValue='" + charValue + '\'' +
                '}';
    }
}