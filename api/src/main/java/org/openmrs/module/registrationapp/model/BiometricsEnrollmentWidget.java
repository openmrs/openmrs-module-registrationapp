package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class BiometricsEnrollmentWidget extends Widget {

    @JsonProperty
    private Config config;

    public BiometricsEnrollmentWidget() {
        super("registrationapp", "field/biometricsEnrollment");
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public static class Config {

        @JsonProperty
        private List<Finger> fingers;

        public void addFinger(Finger finger) {
            if (fingers == null) {
                fingers = new ArrayList<Finger>();
            }
            fingers.add(finger);
        }

        public List<Finger> getFingers() {
            return fingers;
        }

        public void setFingers(List<Finger> fingers) {
            this.fingers = fingers;
        }
    }

    public static class Finger {

        @JsonProperty
        private String type;  // This is what the fingerprinting engine uses to identify a particular finger (eg. left-index)

        @JsonProperty
        private String label;  // This is what should appear on the enrollment page for the user

        public Finger(String type, String label) {
            this.type = type;
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
