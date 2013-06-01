package org.openmrs.module.registrationapp.model;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class Question {

    @JsonProperty
    private String legend;

    @JsonProperty
    private List<Field> fields;

    public Question() {
    }

    public Question(List<Field> fields, String legend) {
        this.fields = fields;
        this.legend = legend;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }
}
