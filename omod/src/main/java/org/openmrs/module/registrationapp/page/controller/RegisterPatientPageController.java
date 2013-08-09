package org.openmrs.module.registrationapp.page.controller;

import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

public class RegisterPatientPageController {

    private static final String REGISTRATION_SECTION_EXTENSION_POINT = "org.openmrs.module.registrationapp.section";
    private static final String REGISTRATION_FORM_STRUCTURE = "formStructure";

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("appId") AppDescriptor app,
                    @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate) throws Exception {

        sessionContext.requireAuthentication();
        addModelAttributes(model, app, nameTemplate);
    }



    public String post(UiSessionContext sessionContext, PageModel model, @RequestParam("appId") AppDescriptor app,
                       @SpringBean("registrationCoreService") RegistrationCoreService registrationService,
                       @ModelAttribute("patient") @BindParams Patient patient,
                       @ModelAttribute("personName") @BindParams PersonName name,
                       @ModelAttribute("personAddress") @BindParams PersonAddress address,
                       @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                       @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                       HttpServletRequest request, @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService, Session session,
                       UiUtils ui) throws Exception {

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
            addModelAttributes(model, app, nameTemplate);
            return null;
        }

        //TODO create encounters
        patient = registrationService.registerPatient(patient, null, sessionContext.getSessionLocation());

        InfoErrorMessageUtil.flashInfoMessage(request.getSession(), ui.message("registrationapp.createdPatientMessage", patient.getPersonName()));

        String redirectUrl = app.getConfig().get("afterCreatedUrl").getTextValue();
        redirectUrl = redirectUrl.replaceAll("\\{\\{patientId\\}\\}", patient.getId().toString());
        return "redirect:" + redirectUrl;
    }


    public void addModelAttributes(PageModel model, AppDescriptor app, NameTemplate nameTemplate) throws Exception {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        model.addAttribute("appId", app.getId());
        model.addAttribute("formStructure", formStructure);
        model.addAttribute("nameTemplate", nameTemplate);
        model.addAttribute("addressTemplate", AddressSupport.getInstance().getAddressTemplate().get(0));
        model.addAttribute("enableOverrideOfAddressPortlet",
                Context.getAdministrationService().getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
    }


}
