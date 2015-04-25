package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class DropdownWidget extends Widget {

    @JsonProperty
    private Config config;

    public DropdownWidget() {
        super("uicommons", "field/dropDown");
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
        private Boolean expanded;

        @JsonProperty
        private Boolean hideEmptyLabel;

        @JsonProperty
        private String initialValue;

        @JsonProperty
        private List<Option> options;

        public Boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(Boolean expanded) {
            this.expanded = expanded;
        }

        public Boolean isHideEmptyLabel() {
            return hideEmptyLabel;
        }

        public void setHideEmptyLabel(Boolean hideEmptyLabel) {
            this.hideEmptyLabel = hideEmptyLabel;
        }

        public String getInitialValue() {
            return initialValue;
        }

        public void setInitialValue(String initialValue) {
            this.initialValue = initialValue;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }

        public void addOption(String value, String label) {
            if (options == null) {
                options = new ArrayList<Option>();
            }
            options.add(new Option(value, label));
        }
    }

    public static class Option {

        @JsonProperty
        private String value;

        @JsonProperty
        private String label;

        public Option() {}

        public Option(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
