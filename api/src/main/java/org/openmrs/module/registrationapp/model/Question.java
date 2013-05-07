package org.openmrs.module.registrationapp.model;


import java.util.List;
import java.util.Map;

public class Question {

    private String label;
    private List<Field> fields;

    public Question() {
    }

    public Question(List<Field> fields, String label) {
        this.fields = fields;
        this.label = label;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
