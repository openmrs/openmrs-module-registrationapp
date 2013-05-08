package org.openmrs.module.registrationapp.page.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.layout.web.name.NameTemplate;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.emr.EmrContext;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationapp.model.Question;
import org.openmrs.module.registrationapp.model.Section;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

public class RegisterPatientPageController {

    private static final String REGISTRATION_SECTION_EXTENSION_POINT = "org.openmrs.module.registrationapp.section";

    public void get(EmrContext emrContext, PageModel model,
        @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
        @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate) {

        emrContext.requireAuthentication();

        NavigableFormStructure formStructure = buildFormStructure(appFrameworkService);

        model.addAttribute("form", formStructure);
        model.addAttribute("nameTemplate", nameTemplate);
    }

    private NavigableFormStructure buildFormStructure(AppFrameworkService appFrameworkService) {
        NavigableFormStructure formStructure = new NavigableFormStructure();

        List<Extension> sections = appFrameworkService.getExtensionsForCurrentUser(REGISTRATION_SECTION_EXTENSION_POINT);
        for (Extension extension : sections) {
            Section section = new Section(extension.getId(), extension.getLabel());
            Map<String, Question> questions = new HashMap<String, Question>();

            List<Map<String, Object>> extQuestions = (List<Map<String, Object>>) extension.getExtensionParams().get("questions");
            for (Map<String, Object> exQuestionMap : extQuestions) {
               Question question = new Question();
               String questionLabel = (String)exQuestionMap.get("label");
               question.setLabel(questionLabel);
               List<Map<String,Object>> exQuestionFields = (List<Map<String, Object>>) exQuestionMap.get("fields");
               if(exQuestionFields!=null){
                   List<Field> fields = new ArrayList<Field>();
                   for (Map<String, Object> exQuestionField : exQuestionFields) {
                       String type =  (String)exQuestionField.get("type");
                       String table = (String)exQuestionField.get("table");
                       String uuid = (String)exQuestionField.get("uuid");
                       String fragment = (String)exQuestionField.get("fragment");
                       Field field = new Field(type, table, uuid, fragment);
                       fields.add(field);
                   }
                   question.setFields(fields);
               }
               questions.put(questionLabel, question);
            }
            section.setQuestions(questions);
            formStructure.addSection(section);
        }

        return formStructure;
    }

    public String post(EmrContext emrContext,
        @SpringBean("registrationCoreService") RegistrationCoreService registrationService,
        @ModelAttribute("patient") @BindParams Patient patient,
        @MethodParam("buildBirthdate") @BindParams Date birthdate,
        @ModelAttribute("personName") @BindParams PersonName name,
        UiUtils ui) {

        patient.setBirthdate(birthdate);
        patient.addName(name);

        //TODO create encounters
        patient = registrationService.registerPatient(patient, null, emrContext.getSessionLocation());
        return "redirect:" + ui.pageLink("emr", "patient?patientId=" + patient.getId().toString());
    }


    public Date buildBirthdate(@RequestParam("birthDay") int birthDay,
        @RequestParam("birthMonth") int birthMonth,
        @RequestParam("birthYear") int birthYear) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(birthYear, birthMonth, birthDay);

        return calendar.getTime();
    }

}
