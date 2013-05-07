package org.openmrs.module.registrationapp.model;

public class Field {
    private String type;
    private String table;
    private String uuid;
    private String fragment;

    public Field() {
    }

    public Field(String type, String table, String uuid, String fragment) {
        this.type = type;
        this.table = table;
        this.uuid = uuid;
        this.fragment = fragment;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
