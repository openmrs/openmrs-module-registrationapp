package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class TextAreaWidget extends Widget {

    @JsonProperty
    private Config config;

    public TextAreaWidget() {
        super("uicommons", "field/textarea");
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
        private Integer maxlength;

        public Integer getMaxlength() {
            return maxlength;
        }

        public void setMaxlength(Integer maxlength) {
            this.maxlength = maxlength;
        }
    }
}
