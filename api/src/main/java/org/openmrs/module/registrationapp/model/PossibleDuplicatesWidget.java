package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The widget of the field of type "possibleDuplicates", the question that reviews possible duplicates of the patient
 * being registered
 */
public class PossibleDuplicatesWidget extends Widget {

    public static final String FIELD_TYPE = "possibleDuplicates";

    @JsonProperty
    private Config config;

    public PossibleDuplicatesWidget() {
        super("registrationapp", "field/possibleDuplicates");
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

        // the name of the form field that starts the check once it has been entered; if not set, the check starts once the
        // question before this one has been answered
        @JsonProperty
        private String triggerField;

        public String getTriggerField() {
            return triggerField;
        }

        public void setTriggerField(String triggerField) {
            this.triggerField = triggerField;
        }
    }
}
