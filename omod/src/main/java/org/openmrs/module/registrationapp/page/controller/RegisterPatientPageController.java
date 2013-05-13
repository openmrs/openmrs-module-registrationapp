package org.openmrs.module.registrationapp.page.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
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
import org.openmrs.ui.framework.fragment.FragmentRequest;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

public class RegisterPatientPageController {

    private static final String REGISTRATION_SECTION_EXTENSION_POINT = "org.openmrs.module.registrationapp.section";
    private static final String REGISTRATION_FORM_STRUCTURE = "formStructure";

    public void get(EmrContext emrContext, PageModel model,
        @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
        @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate) {

        NavigableFormStructure formStructure=getRegistrationFormStructure(emrContext, appFrameworkService);

        model.addAttribute("formStructure", formStructure);
        model.addAttribute("nameTemplate", nameTemplate);
        model.addAttribute("addressTemplate", AddressSupport.getInstance().getAddressTemplate().get(0));
        model.addAttribute("enableOverrideOfAddressPortlet", Context.getAdministrationService().getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
    }

    private NavigableFormStructure getRegistrationFormStructure(EmrContext emrContext, AppFrameworkService appFrameworkService) {
        emrContext.requireAuthentication();
        NavigableFormStructure registrationFormStructure = (NavigableFormStructure)emrContext.getSessionAttribute(REGISTRATION_FORM_STRUCTURE);
        if(registrationFormStructure==null){
            registrationFormStructure = buildFormStructure(appFrameworkService);
            emrContext.setSessionAttribute(REGISTRATION_FORM_STRUCTURE, registrationFormStructure);
        }
        return registrationFormStructure;
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
               String legend = (String)exQuestionMap.get("legend");
               question.setLegend(legend);
               List<Map<String,Object>> exQuestionFields = (List<Map<String, Object>>) exQuestionMap.get("fields");
               if(exQuestionFields!=null){
                   List<Field> fields = new ArrayList<Field>();
                   for (Map<String, Object> exQuestionField : exQuestionFields) {
                       Field field = new Field();
                       String type =  (String)exQuestionField.get("type");
                       if(StringUtils.isNotBlank(type)){
                           field.setType(type);
                       }
                       String label =  (String)exQuestionField.get("label");
                       if(StringUtils.isNotBlank(type)){
                           field.setLabel(label);
                       }
                       String formFieldName = (String)exQuestionField.get("formFieldName");
                       if(StringUtils.isNotBlank(formFieldName)){
                           field.setFormFieldName(formFieldName);
                       }
                       String uuid = (String)exQuestionField.get("uuid");
                       if(StringUtils.isNotBlank(uuid)){
                           field.setUuid(uuid);
                       }
                       Map<String, Object> widget = (Map<String, Object>)exQuestionField.get("widget");
                       if(widget!=null){
                           String providerName = (String)widget.get("providerName");
                           String fragmentId = (String)widget.get("fragmentId");
                           FragmentRequest fragmentRequest = new FragmentRequest(providerName, fragmentId);
                           field.setFragmentRequest(fragmentRequest);
                       }
                       fields.add(field);
                   }
                   question.setFields(fields);
               }
               questions.put(legend, question);
            }
            section.setQuestions(questions);
            formStructure.addSection(section);
        }

        return formStructure;
    }

    public String post(EmrContext emrContext,
        @SpringBean("registrationCoreService") RegistrationCoreService registrationService,
        @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
        @ModelAttribute("patient") @BindParams Patient patient,
        @MethodParam("buildBirthdate") @BindParams Date birthdate,
        @ModelAttribute("personName") @BindParams PersonName name,
        @ModelAttribute("personAddress") @BindParams PersonAddress address,
        HttpServletRequest request,
        UiUtils ui) {

       NavigableFormStructure formStructure = getRegistrationFormStructure(emrContext, appFrameworkService);

        patient.setBirthdate(birthdate);
        patient.addName(name);
        patient.addAddress(address);

        if(formStructure!=null){
            List<Field> fields = formStructure.getFields();
            if(fields!=null && fields.size()>0){
                patient = parseRequestFields(patient, request, fields);
            }
        }

        //TODO create encounters
        patient = registrationService.registerPatient(patient, null, emrContext.getSessionLocation());
        return "redirect:" + ui.pageLink("emr", "patient?patientId=" + patient.getId().toString());
    }


    private Patient parseRequestFields(Patient patient, HttpServletRequest request, List<Field> fields) {
        if(fields!=null && fields.size()>0){
            for (Field field : fields) {
                String parameterValue = request.getParameter(field.getFormFieldName());
                if(StringUtils.isNotBlank(parameterValue)){
                    if(StringUtils.equals(field.getType(), "personAttribute")){
                        PersonAttributeType personAttributeByUuid = Context.getPersonService().getPersonAttributeTypeByUuid(field.getUuid());
                        if(personAttributeByUuid!=null){
                            PersonAttribute attribute = new PersonAttribute(personAttributeByUuid, parameterValue);
                            patient.addAttribute(attribute);
                        }
                    }
                }
            }
        }
        return patient;
    }

    public Date buildBirthdate(@RequestParam("birthDay") int birthDay,
        @RequestParam("birthMonth") int birthMonth,
        @RequestParam("birthYear") int birthYear) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(birthYear, birthMonth, birthDay);

        return calendar.getTime();
    }

}
