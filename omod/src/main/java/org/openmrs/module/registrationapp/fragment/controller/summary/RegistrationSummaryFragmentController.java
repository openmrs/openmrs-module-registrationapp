package org.openmrs.module.registrationapp.fragment.controller.summary;


import org.openmrs.Patient;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Collections;
import java.util.List;

public class RegistrationSummaryFragmentController {

    public void controller(FragmentConfiguration config,
                           FragmentModel model,
                           @SpringBean AppFrameworkService appFrameworkService,
                           @InjectBeans PatientDomainWrapper patientDomainWrapper
                           ) {

        config.require("patient");

        Object patient = config.get("patient");
        if (patient instanceof Patient) {
            patientDomainWrapper.setPatient((Patient) patient);
        } else if (patient instanceof PatientDomainWrapper) {
            patientDomainWrapper = (PatientDomainWrapper) patient;
        }
        model.addAttribute("patient", patientDomainWrapper);

        AppDescriptor app = null;
        if (config.get("appId") !=null ) {
            app = appFrameworkService.getApp((String) config.get("appId"));
        }
        model.addAttribute("appId", app !=null ? app.getId() : null);

        List<Extension> registrationFragments = appFrameworkService.getExtensionsForCurrentUser("registrationSummary.contentFragments");
        Collections.sort(registrationFragments);
        model.addAttribute("registrationFragments", registrationFragments);

    }
}
