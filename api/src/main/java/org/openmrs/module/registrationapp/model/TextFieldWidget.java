package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class TextFieldWidget extends Widget {

    @JsonProperty
    private Config config;

    public TextFieldWidget() {
        super("uicommons", "field/text");
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
        private Integer size;

        @JsonProperty
        private String regex;

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }
    }
}
