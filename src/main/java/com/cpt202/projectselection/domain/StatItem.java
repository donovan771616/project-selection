package com.cpt202.projectselection.domain;

// Basic data holder for chart label/value pairs
public class StatItem {

    private String label;
    private Integer value;

    public StatItem() {
    }

    public StatItem(String label, Integer value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
