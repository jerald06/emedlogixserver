package com.emedlogix.entity;

import java.util.ArrayList;
import java.util.List;

public class VisualImpairment {

    String categoryHeading;
    String rangeHeading;
    String minHeading;
    String maxHeading;
    List<Category> categoriesList;

    public String getCategoryHeading() {
        return categoryHeading;
    }

    public void setCategoryHeading(String categoryHeading) {
        this.categoryHeading = categoryHeading;
    }

    public String getRangeHeading() {
        return rangeHeading;
    }

    public void setRangeHeading(String rangeHeading) {
        this.rangeHeading = rangeHeading;
    }

    public String getMinHeading() {
        return minHeading;
    }

    public void setMinHeading(String minHeading) {
        this.minHeading = minHeading;
    }

    public String getMaxHeading() {
        return maxHeading;
    }

    public void setMaxHeading(String maxHeading) {
        this.maxHeading = maxHeading;
    }

    public List<Category> getCategoriesList() {
        categoriesList = categoriesList == null ? new ArrayList<>() : categoriesList;
        return categoriesList;
    }

    public void setCategoriesList(List<Category> categoriesList) {
        this.categoriesList = categoriesList;
    }

    @Override
    public String toString() {
        return "VisualImpairment{" +
                "categoryHeading='" + categoryHeading + '\'' +
                ", rangeHeading='" + rangeHeading + '\'' +
                ", minHeading='" + minHeading + '\'' +
                ", maxHeading='" + maxHeading + '\'' +
                ", categoriesList=" + categoriesList +
                '}';
    }
}
