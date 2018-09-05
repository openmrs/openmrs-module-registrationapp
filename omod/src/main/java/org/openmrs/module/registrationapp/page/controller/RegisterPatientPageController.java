package org.openmrs.module.registrationapp.page.controller;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.registrationapp.AddressSupportCompatibility;
import org.openmrs.module.registrationapp.NameSupportCompatibility;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Collections;

public class RegisterPatientPageController extends AbstractRegistrationAppPageController {

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("appId") AppDescriptor app,
                    @RequestParam(value = "patientId", required = false) @BindParams Patient existingPatient,
                    @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
                    @ModelAttribute("patient") @BindParams Patient patient,
                    @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                    @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                    UiUtils ui) throws Exception {

        sessionContext.requireAuthentication();
        addModelAttributes(model, patient, app, emrApiProperties.getPrimaryIdentifierType(), breadcrumbOverride, appFrameworkService, existingPatient);
    }

    public void addModelAttributes(PageModel model, Patient patient, AppDescriptor app, PatientIdentifierType primaryIdentifierType, String breadcrumbOverride, AppFrameworkService appFrameworkService, Patient existingPatient) throws Exception {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        if (patient == null) {
          patient = new Patient();
        } else {
          patient = existingPatient;
        }

        NameSupportCompatibility nameSupport = Context.getRegisteredComponent(NameSupportCompatibility.ID, NameSupportCompatibility.class);
        AddressSupportCompatibility addressSupport = Context.getRegisteredComponent(AddressSupportCompatibility.ID, AddressSupportCompatibility.class);
        
        model.addAttribute("patient", patient);
        model.addAttribute("patienIdNumber", patient.getId());
        model.addAttribute("primaryIdentifierType", primaryIdentifierType);
        model.addAttribute("appId", app.getId());
        model.addAttribute("formStructure", formStructure);
        model.addAttribute("nameTemplate", nameSupport.getDefaultLayoutTemplate());
        model.addAttribute("addressTemplate", addressSupport.getAddressTemplate());
        model.addAttribute("includeRegistrationDateSection", !app.getConfig().get("registrationEncounter").isNull()
                && !app.getConfig().get("allowRetrospectiveEntry").isNull()
                && app.getConfig().get("allowRetrospectiveEntry").getBooleanValue() );
        model.addAttribute("allowUnknownPatients", app.getConfig().get("allowUnknownPatients").getBooleanValue());
        model.addAttribute("allowManualIdentifier", app.getConfig().get("allowManualIdentifier").getBooleanValue());
        model.addAttribute("patientDashboardLink", app.getConfig().get("patientDashboardLink") !=null ?
                app.getConfig().get("patientDashboardLink").getTextValue() : null);
        model.addAttribute("enableOverrideOfAddressPortlet",
                Context.getAdministrationService().getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
        model.addAttribute("breadcrumbOverride", breadcrumbOverride);
        model.addAttribute("relationshipTypes", Context.getPersonService().getAllRelationshipTypes());

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("registerPatient.includeFragments");
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);
    }

}
