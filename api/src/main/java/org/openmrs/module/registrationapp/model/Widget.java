package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class Widget {

    @JsonProperty
    private String providerName;

    @JsonProperty
    private String fragmentId;

    public Widget() {
    }

    public Widget(String providerName, String fragmentId) {
        this.providerName = providerName;
        this.fragmentId = fragmentId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(String fragmentId) {
        this.fragmentId = fragmentId;
    }
}
