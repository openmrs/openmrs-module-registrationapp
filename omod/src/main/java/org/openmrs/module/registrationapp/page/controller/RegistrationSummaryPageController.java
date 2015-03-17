package org.openmrs.module.registrationapp.page.controller;


import org.openmrs.Patient;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

public class RegistrationSummaryPageController {

    public Object controller(@RequestParam("patientId") Patient patient, PageModel model,
                             @RequestParam(value="appId", required = false) AppDescriptor app,
                             @InjectBeans PatientDomainWrapper patientDomainWrapper,
                             @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
                             UiSessionContext sessionContext) {

        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("patient", patientDomainWrapper);
        model.addAttribute("appId", app !=null ? app.getId() : null);

        applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

        return null;

    }
}
