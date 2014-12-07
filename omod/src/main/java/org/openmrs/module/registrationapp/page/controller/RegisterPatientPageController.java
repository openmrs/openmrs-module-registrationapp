package org.openmrs.module.registrationapp.page.controller;

import org.joda.time.DateTimeComparator;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.layout.web.name.NameTemplate;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.uicommons.UiCommonsConstants;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.openmrs.validator.PatientValidator;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

public class RegisterPatientPageController {

    private static final String REGISTRATION_SECTION_EXTENSION_POINT = "org.openmrs.module.registrationapp.section";
    private static final String REGISTRATION_FORM_STRUCTURE = "formStructure";

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("appId") AppDescriptor app, @ModelAttribute("patient") @BindParams Patient patient,
                    @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate) throws Exception {

        sessionContext.requireAuthentication();
        addModelAttributes(model, patient, app, nameTemplate);
    }


    public String post(UiSessionContext sessionContext, PageModel model, @RequestParam("appId") AppDescriptor app,
                       @SpringBean("registrationCoreService") RegistrationCoreService registrationService,
                       @ModelAttribute("patient") @BindParams Patient patient,
                       @ModelAttribute("personName") @BindParams PersonName name,
                       @ModelAttribute("personAddress") @BindParams PersonAddress address,
                       @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                       @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                       @RequestParam(value="registrationDate", required = false) Date registrationDate,
                       HttpServletRequest request, @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService,
                       @SpringBean("encounterService") EncounterService encounterService,
                       Session session,
                       @SpringBean("patientValidator") PatientValidator patientValidator, UiUtils ui) throws Exception {

        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        patient.addName(name);
        patient.addAddress(address);

        if (patient.getBirthdate() == null) {
            patient.setBirthdateEstimated(true);
            Calendar calendar = Calendar.getInstance();
            if (birthdateYears != null) {
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - birthdateYears);
            }
            if (birthdateMonths != null) {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - birthdateMonths);
            }
            patient.setBirthdate(calendar.getTime());
        }

        if(formStructure!=null){
        	RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
        }

        BindingResult errors = new BeanPropertyBindingResult(patient, "patient");
        //TODO This validation code really belongs to the PersonAddressValidator in core
        RegistrationAppUiUtils.validateLatitudeAndLongitudeIfNecessary(address, errors);

        if (errors.hasErrors()) {
            addModelErrors(model, patient, errors, session, app, nameTemplate, messageSourceService);
            return null;
        }

        try {
            // note that this will only create an encounter if getRegistrationEncounter != null
        	patient = registrationService.registerPatient(patient, null, sessionContext.getSessionLocation());
        }
        catch (Exception ex) {
        	//TODO I remember getting into trouble if i called this validator before the above save method.
        	//Am therefore putting this here for: https://tickets.openmrs.org/browse/RA-232
        	patientValidator.validate(patient, errors);
        	if (!errors.hasErrors()) {
        		errors.reject(ex.getMessage());
        	}
        	addModelErrors(model, patient, errors, session, app, nameTemplate, messageSourceService);
        	return null;
        }

        // now create the registration encounter, if configured to do so
        Encounter registrationEncounter = buildRegistrationEncounter(patient, registrationDate, sessionContext, app, encounterService);
        if (registrationEncounter != null) {
            encounterService.saveEncounter(registrationEncounter);
        }

        InfoErrorMessageUtil.flashInfoMessage(request.getSession(), ui.message("registrationapp.createdPatientMessage", patient.getPersonName()));

        String redirectUrl = app.getConfig().get("afterCreatedUrl").getTextValue();
        redirectUrl = redirectUrl.replaceAll("\\{\\{patientId\\}\\}", patient.getId().toString());
        return "redirect:" + redirectUrl;
    }

    private void addModelErrors(PageModel model, Patient patient, BindingResult errors, Session session, AppDescriptor app, NameTemplate nameTemplate, MessageSourceService messageSourceService) throws Exception {
    	model.addAttribute("errors", errors);
        StringBuffer errorMessage = new StringBuffer(messageSourceService.getMessage("error.failed.validation"));
        errorMessage.append("<ul>");
        for (ObjectError error : errors.getAllErrors()) {
            errorMessage.append("<li>");
            errorMessage.append(messageSourceService.getMessage(error.getCode(), error.getArguments(),
                    error.getDefaultMessage(), null));
            errorMessage.append("</li>");
        }
        errorMessage.append("</ul>");
        session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, errorMessage.toString());

        //send the user back to the form to fix errors
        addModelAttributes(model, patient, app, nameTemplate);
    }

    private Encounter buildRegistrationEncounter(Patient patient, Date registrationDate, UiSessionContext sessionContext, AppDescriptor app, EncounterService encounterService) {

        EncounterType registrationEncounterType = getRegistrationEncounterType(app, encounterService);
        EncounterRole registrationEncounterRole = getRegistrationEncounteRole(app, encounterService);

        // if no registration encounter type specified, we aren't configured to create an encounter
        if (registrationEncounterType == null) {
            return null;
        }

        // if date not specified, or date = current date, consider this a "real-time" entry, and set date to current time
        if (DateTimeComparator.getDateOnlyInstance().compare(registrationDate, new Date()) == 0){
            registrationDate = new Date();
        }

        // create the encounter
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setEncounterType(registrationEncounterType);
        encounter.setLocation(sessionContext.getSessionLocation());
        encounter.setEncounterDatetime(registrationDate);

       if (registrationEncounterRole != null) {
           encounter.addProvider(registrationEncounterRole, sessionContext.getCurrentProvider());
       }

       return encounter;
    }


    private EncounterType getRegistrationEncounterType(AppDescriptor app, EncounterService encounterService) {

        EncounterType registrationEncounterType = null;

        if (!app.getConfig().get("registrationEncounterType").isNull()) {

            String encounterTypeUuid = app.getConfig().get("registrationEncounterType").getTextValue();
            registrationEncounterType = encounterService.getEncounterTypeByUuid(encounterTypeUuid);

            if (registrationEncounterType == null) {
                throw new IllegalArgumentException("Cannot find encounter type referenced by " + encounterTypeUuid);
            }
        }

        return registrationEncounterType;
    }

    private EncounterRole getRegistrationEncounteRole(AppDescriptor app, EncounterService encounterService) {

        EncounterRole registrationEncounterRole = null;

        if (!app.getConfig().get("registrationEncounterRole").isNull()) {

            String encounterRoleUuid = app.getConfig().get("registrationEncounterRole").getTextValue();
            registrationEncounterRole = encounterService.getEncounterRoleByUuid(encounterRoleUuid);

            if (registrationEncounterRole == null) {
                throw new IllegalArgumentException("Cannot find encounter type referenced by " + encounterRoleUuid);
            }
        }

        return registrationEncounterRole;
    }

    public void addModelAttributes(PageModel model, Patient patient, AppDescriptor app, NameTemplate nameTemplate) throws Exception {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        if (patient == null) {
        	patient = new Patient();
        }
        
        model.addAttribute("patient", patient);
        model.addAttribute("appId", app.getId());
        model.addAttribute("formStructure", formStructure);
        model.addAttribute("nameTemplate", nameTemplate);
        model.addAttribute("addressTemplate", AddressSupport.getInstance().getAddressTemplate().get(0));
        model.addAttribute("showRegistrationDateSection", !app.getConfig().get("registrationEncounterType").isNull()
                && !app.getConfig().get("allowRetrospectiveEntry").isNull()
                && app.getConfig().get("allowRetrospectiveEntry").getBooleanValue() );
        model.addAttribute("enableOverrideOfAddressPortlet",
                Context.getAdministrationService().getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
    }

}
