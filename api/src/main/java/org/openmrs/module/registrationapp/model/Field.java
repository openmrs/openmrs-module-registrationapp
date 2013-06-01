package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.ui.framework.fragment.FragmentRequest;

public class Field {

    @JsonProperty
    private String type;

    @JsonProperty
    private String label;

    @JsonProperty
    private String formFieldName;

    @JsonProperty
    private String uuid;

    @JsonProperty
    private ObjectNode widget;

    private transient FragmentRequest fragmentRequest;

    public Field() {
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

    public ObjectNode getWidget() {
        return widget;
    }

    public void setWidget(ObjectNode widget) {
        this.widget = widget;
    }
}
