package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Section {

    @JsonProperty
    private String id;

    @JsonProperty
    private String label;

    @JsonProperty
    private List<Question> questions;

    public Section() {
    }

    public Section(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public Section(String id, String label, List<Question> questions) {
        this.id = id;
        this.label = label;
        this.questions = questions;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question) {
        if (questions == null) {
            questions = new ArrayList<Question>();
        }
        questions.add(question);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
