package org.openmrs.module.registrationapp.model;

import org.openmrs.ui.framework.fragment.FragmentRequest;

public class Field {
    private String type;
    private String label;
    private String formFieldName;
    private String uuid;
    private FragmentRequest fragmentRequest;

    public Field() {
    }

    public Field(String type, String label, String formFieldName, String uuid, FragmentRequest fragmentRequest) {
        this.type = type;
        this.label = label;
        this.formFieldName = formFieldName;
        this.uuid = uuid;
        this.fragmentRequest =fragmentRequest;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFormFieldName() {
        return formFieldName;
    }

    public void setFormFieldName(String formFieldName) {
        this.formFieldName = formFieldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public FragmentRequest getFragmentRequest() {
        return fragmentRequest;
    }

    public void setFragmentRequest(FragmentRequest fragmentRequest) {
        this.fragmentRequest = fragmentRequest;
    }
}
