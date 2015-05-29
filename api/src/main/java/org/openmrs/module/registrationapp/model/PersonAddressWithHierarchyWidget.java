package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonAddressWithHierarchyWidget extends Widget {

    @JsonProperty
    private Config config = new Config();

    public PersonAddressWithHierarchyWidget() {
        super("registrationapp", "field/personAddressWithHierarchy");
    }

    public Config getConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public static class Config {

        @JsonProperty
        private String shortcutFor;

        @JsonProperty
        private List<String> manualFields;

        @JsonProperty
        private Map<String, String> fieldMappings;

        @JsonProperty
        private Boolean required;

        public Config() {
        }

        public String getShortcutFor() {
            return shortcutFor;
        }

        public void setShortcutFor(String shortcutFor) {
            this.shortcutFor = shortcutFor;
        }

        public List<String> getManualFields() {
            return manualFields;
        }

        public void setManualFields(List<String> manualFields) {
            this.manualFields = manualFields;
        }

        public void addManualField(String field) {
            if (manualFields == null) {
                manualFields = new ArrayList<String>();
            }
            manualFields.add(field);
        }

        public Map<String, String> getFieldMappings() {
            return fieldMappings;
        }

        public void setFieldMappings(Map<String, String> fieldMappings) {
            this.fieldMappings = fieldMappings;
        }

        public void addFieldMapping(String key, String value) {
            if (fieldMappings == null) {
                fieldMappings = new HashMap<String, String>();
            }
            fieldMappings.put(key, value);
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }
    }
}
