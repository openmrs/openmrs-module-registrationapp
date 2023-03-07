package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class CheckboxWidget extends Widget {

    @JsonProperty
    private Config config;

    public CheckboxWidget( ){ super("uicommons", "field/checkbox"); }

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
        private Boolean checked;

        @JsonProperty
        private String value;

        public Boolean getChecked() {
            return checked;
        }

        public void setChecked(Boolean checked) {
            this.checked = checked;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
