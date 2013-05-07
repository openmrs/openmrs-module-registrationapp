package org.openmrs.module.registrationapp.model;

import java.util.Map;

public class Section {
    private String id;
    private String label;
    private Map<String, Question> questions;

    public Section() {
    }

    public Section(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public Section(String id, String label, Map<String, Question> questions) {
        this.id = id;
        this.label = label;
        this.questions = questions;
    }

    public Map<String, Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Map<String, Question> questions) {
        this.questions = questions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
