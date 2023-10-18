package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class RegisterPersonRelationshipWidget extends Widget{

    private Config config = new Config();

    public RegisterPersonRelationshipWidget() {
        super("registrationapp", "field/registerPersonRelationship");
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
        private String type;
        @JsonProperty
        private String relationshipType;

        @JsonProperty
        private String gender;

        @JsonProperty
        private Boolean multipleValues;

        @JsonProperty
        private Boolean required;
        public Config() {}

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }

        public Boolean getMultipleValues() {
            return multipleValues;
        }

        public void setMultipleValues(Boolean multipleValues) {
            this.multipleValues = multipleValues;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }
    }
}
