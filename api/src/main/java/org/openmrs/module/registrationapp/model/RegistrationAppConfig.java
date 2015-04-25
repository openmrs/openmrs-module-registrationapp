package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class RegistrationAppConfig {

    @JsonProperty
    private String afterCreatedUrl;

    @JsonProperty
    private String patientDashboardLink;

    @JsonProperty
    private boolean allowRetrospectiveEntry;

    @JsonProperty
    private boolean allowUnknownPatients;

    @JsonProperty
    private boolean allowManualIdentifier;

    @JsonProperty
    private RegistrationEncounter registrationEncounter;

    @JsonProperty
    private List<Section> sections;

    public RegistrationAppConfig() {
    }

    public String getAfterCreatedUrl() {
        return afterCreatedUrl;
    }

    public void setAfterCreatedUrl(String afterCreatedUrl) {
        this.afterCreatedUrl = afterCreatedUrl;
    }

    public String getPatientDashboardLink() {
        return patientDashboardLink;
    }

    public void setPatientDashboardLink(String patientDashboardLink) {
        this.patientDashboardLink = patientDashboardLink;
    }

    public boolean isAllowRetrospectiveEntry() {
        return allowRetrospectiveEntry;
    }

    public void setAllowRetrospectiveEntry(boolean allowRetrospectiveEntry) {
        this.allowRetrospectiveEntry = allowRetrospectiveEntry;
    }

    public boolean isAllowUnknownPatients() {
        return allowUnknownPatients;
    }

    public void setAllowUnknownPatients(boolean allowUnknownPatients) {
        this.allowUnknownPatients = allowUnknownPatients;
    }

    public boolean isAllowManualIdentifier() {
        return allowManualIdentifier;
    }

    public void setAllowManualIdentifier(boolean allowManualIdentifier) {
        this.allowManualIdentifier = allowManualIdentifier;
    }

    public RegistrationEncounter getRegistrationEncounter() {
        return registrationEncounter;
    }

    public void setRegistrationEncounter(RegistrationEncounter registrationEncounter) {
        this.registrationEncounter = registrationEncounter;
    }

    public void setRegistrationEncounter(String encounterType, String encounterRole) {
        setRegistrationEncounter(new RegistrationEncounter(encounterType, encounterRole));
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public void addSection(Section section) {
        if (sections == null) {
            sections = new ArrayList<Section>();
        }
        sections.add(section);
    }

    public static class RegistrationEncounter {

        @JsonProperty
        private String encounterType;

        @JsonProperty
        private String encounterRole;

        public RegistrationEncounter() {}

        public RegistrationEncounter(String encounterType, String encounterRole) {
            this.encounterType = encounterType;
            this.encounterRole = encounterRole;
        }

        public String getEncounterType() {
            return encounterType;
        }

        public void setEncounterType(String encounterType) {
            this.encounterType = encounterType;
        }

        public String getEncounterRole() {
            return encounterRole;
        }

        public void setEncounterRole(String encounterRole) {
            this.encounterRole = encounterRole;
        }
    }
}
