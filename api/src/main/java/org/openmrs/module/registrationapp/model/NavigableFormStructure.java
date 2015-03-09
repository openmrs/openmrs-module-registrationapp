package org.openmrs.module.registrationapp.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class NavigableFormStructure {
    private Map<String, Section> sections;

    public NavigableFormStructure() {
        sections = new LinkedHashMap<String, Section>();
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
    public List<Field> getFields(){
        List<Field> fields = null;
        if(sections!=null && sections.size()>0){
            fields = new ArrayList<Field>();
            for (Section section : sections.values()) {
                Map<String, Question> questions = new LinkedHashMap<String, Question>();
                if (section.getQuestions() != null) {
                    for (Question question : section.getQuestions()) {
                        List<Field> qFields = question.getFields();
                        if(qFields!=null && qFields.size()>0){
                            fields.addAll(qFields);
                        }
                    }
                }
            }
        }

        return fields;
    }

}
