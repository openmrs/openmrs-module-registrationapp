package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

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
    }
}
