package org.openmrs.module.registrationapp.model;

import org.openmrs.module.appframework.domain.Extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NavigableFormStructure {
    private Map<String, Section> sections;

    public NavigableFormStructure() {
        sections = new HashMap<String, Section>();
    }

    public Map<String, Section> getSections() {
        return sections;
    }

    public void setSections(Map<String, Section> sections) {
        this.sections = sections;
    }

    public void addSection(Section section) {
        sections.put(section.getId(), section);
    }

}
