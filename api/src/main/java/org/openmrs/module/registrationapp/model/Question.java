package org.openmrs.module.registrationapp.model;


import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.module.appframework.domain.Requireable;

import java.util.ArrayList;
import java.util.List;

public class Question implements Requireable {

    @JsonProperty
    private String legend;

    @JsonProperty
    private String id;

    @JsonProperty
    private String fieldSeparator;

    @JsonProperty
    private String displayTemplate;
    
    @JsonProperty
    private List<Field> fields;

    @JsonProperty
    private String header;

    @JsonProperty
    private List<String> cssClasses;

    @JsonProperty
    private String require;

    public Question() {
    }

    public Question(List<Field> fields, String legend) {
        this.fields = fields;
        this.legend = legend;
    }

    public Question(List<Field> fields, String legend, String id) {
    	this.fields = fields;
    	this.legend = legend;
    	this.id = id;
    }

    public Question(List<Field> fields, String legend, String header, String id) {
        this.fields = fields;
        this.legend = legend;
        this.header = header;
        this.id = id;
    }
    
    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void addField(Field field) {
        if (fields == null) {
            fields = new ArrayList<Field>();
        }
        fields.add(field);
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public String getHeader() { return header; }

    public void setHeader(String header) { this.header = header; }

    public String getId() {
    	return id;
    }
	
    public void setId(String id) {
    	this.id = id;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getDisplayTemplate() {
        return displayTemplate;
    }

    public void setDisplayTemplate(String displayTemplate) {
        this.displayTemplate = displayTemplate;
    }

    public List<String> getCssClasses() {
        return cssClasses;
    }

    public void setCssClasses(List<String> cssClasses) {
        this.cssClasses = cssClasses;
    }

	@Override
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

		Question question = (Question) o;

		return id != null ? id.equals(question.id) : question.id == null;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
