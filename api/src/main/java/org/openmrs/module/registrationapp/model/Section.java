package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.module.appframework.domain.Requireable;

import java.util.ArrayList;
import java.util.List;

public class Section implements Requireable {

    @JsonProperty
    private String id;

    @JsonProperty
    private String label;

    @JsonProperty
    private List<Question> questions;

    @JsonProperty
    private Boolean skipConfirmation;   // true/false, when editing this section on it's own, should we render a confirmation page?

    @JsonProperty
    protected String require;

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

    public Section(String id, String label, List<Question> questions, Boolean skipConfirmation) {
        this.id = id;
        this.label = label;
        this.questions = questions;
        this.skipConfirmation = skipConfirmation;
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

    public Boolean getSkipConfirmation() {
        return skipConfirmation;
    }

    public void setSkipConfirmation(Boolean skipConfirmation) {
        this.skipConfirmation = skipConfirmation;
    }

    public String getRequire() {
        return require;
    }

    public void setRequire(String require) {
        this.require = require;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Section section = (Section) o;

        return id != null ? id.equals(section.id) : section.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
