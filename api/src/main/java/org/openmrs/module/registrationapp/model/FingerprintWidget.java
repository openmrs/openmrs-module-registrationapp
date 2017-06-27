package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class FingerprintWidget extends Widget {

    @JsonProperty
    private Config config;

    public FingerprintWidget() {
        super("registrationapp", "field/fingerprint");
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public static class Config {

        @JsonProperty
        private List<FingerprintFormField> fingers;

        public void addFinger(FingerprintFormField finger) {
            if (fingers == null) {
                fingers = new ArrayList<FingerprintFormField>();
            }
            fingers.add(finger);
        }

        public List<FingerprintFormField> getFingers() {
            return fingers;
        }

        public void setFingers(List<FingerprintFormField> fingers) {
            this.fingers = fingers;
        }
    }

    public static class FingerprintFormField {

        @JsonProperty
        private String formFieldName;

        @JsonProperty
        private String label;  // This is what should appear on the enrollment page for the user

        @JsonProperty
        private String type;  // This is what the fingerprinting engine uses to identify a particular finger (eg. left-index)

        public FingerprintFormField(String formFieldName, String label, String type) {
            this.formFieldName = formFieldName;
            this.label = label;
            this.type = type;
        }

        public String getFormFieldName() {
            return formFieldName;
        }

        public void setFormFieldName(String formFieldName) {
            this.formFieldName = formFieldName;
        }


        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
