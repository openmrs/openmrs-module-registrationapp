package org.openmrs.module.registrationapp.page.controller;


import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

public class RegistrationSummaryPageController {

    public Object controller(@RequestParam("patientId") Patient patient, PageModel model,
                             @RequestParam(value="appId", required = false) AppDescriptor app,
                             @InjectBeans PatientDomainWrapper patientDomainWrapper,
                             @SpringBean("encounterService") EncounterService encounterService,
                             @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                             @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                             @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
                             UiSessionContext sessionContext) {

        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("patient", patientDomainWrapper);

        Location visitLocation = sessionContext.getSessionLocation();

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.includeFragments");
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);


        List<Extension> registrationFragments = appFrameworkService.getExtensionsForCurrentUser("registrationSummary.contentFragments");
        Collections.sort(registrationFragments);
        model.addAttribute("registrationFragments", registrationFragments);
        model.addAttribute("appId", app !=null ? app.getId() : null);

        applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

        return null;

    }
}
