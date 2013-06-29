package org.openmrs.module.registrationapp.page.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.layout.web.name.NameTemplate;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

public class RegisterPatientPageController {

    private static final String REGISTRATION_SECTION_EXTENSION_POINT = "org.openmrs.module.registrationapp.section";
    private static final String REGISTRATION_FORM_STRUCTURE = "formStructure";

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("appId") AppDescriptor app,
                    @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate) throws Exception {

        sessionContext.requireAuthentication();
        addModelAttributes(model, app, nameTemplate);
    }

    

    public String post(UiSessionContext sessionContext,
                       PageModel model,
                       @RequestParam("appId") AppDescriptor app,
                       @SpringBean("registrationCoreService") RegistrationCoreService registrationService,
                       @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService,
                       @ModelAttribute("patient") @BindParams Patient patient,
                       @ModelAttribute("personName") @BindParams PersonName name,
                       @ModelAttribute("personAddress") @BindParams PersonAddress address,
                       @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate, HttpServletRequest request,
                       Session Session, UiUtils ui) throws Exception {

        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        patient.addName(name);
        patient.addAddress(address);

        if(formStructure!=null){
        	RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
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

        model.addAttribute("formStructure", formStructure);
        model.addAttribute("nameTemplate", nameTemplate);
        model.addAttribute("addressTemplate", AddressSupport.getInstance().getAddressTemplate().get(0));
        model.addAttribute("enableOverrideOfAddressPortlet",
                Context.getAdministrationService().getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
    }


}
