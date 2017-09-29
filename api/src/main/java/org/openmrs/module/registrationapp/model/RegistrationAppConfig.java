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

    @JsonProperty
    private List<String> afterCreatedActions;

    // properties to display when displaying lists of similar or exact patient matches
    @JsonProperty
    private List<String> matchingPatientsPropertiesToDisplay;

    // identifier types (specified by uuid or name) to display when displaying lists of similar or exact patient matches
    // (if null, displays all identifiers)
    @JsonProperty
    private List<String> identifierTypesToDisplay;

    // maximum number of patients to return when doing similar and exact searches (default is 10)
    @JsonProperty
    private Integer maxPatientSearchResults;

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

    public List<String> getMatchingPatientsPropertiesToDisplay() {
        return matchingPatientsPropertiesToDisplay;
    }

    public void setMatchingPatientsPropertiesToDisplay(List<String> matchingPatientsPropertiesToDisplay) {
        this.matchingPatientsPropertiesToDisplay = matchingPatientsPropertiesToDisplay;
    }

    public List<String> getIdentifierTypesToDisplay() {
        return identifierTypesToDisplay;
    }

    public void setIdentifierTypesToDisplay(List<String> identifierTypesToDisplay) {
        this.identifierTypesToDisplay = identifierTypesToDisplay;
    }

    public Integer getMaxPatientSearchResults() {
        return maxPatientSearchResults;
    }

    public void setMaxPatientSearchResults(Integer maxPatientSearchResults) {
        this.maxPatientSearchResults = maxPatientSearchResults;
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

    public List<String> getAfterCreatedActions() {
        return afterCreatedActions;
    }

    public void setAfterCreatedActions(List<String> afterCreatedActions) {
        this.afterCreatedActions = afterCreatedActions;
    }

    public void addAfterCreatedAction(String afterCreatedAction) {
        if (afterCreatedActions == null) {
            afterCreatedActions = new ArrayList<String>();
        }
        afterCreatedActions.add(afterCreatedAction);
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
