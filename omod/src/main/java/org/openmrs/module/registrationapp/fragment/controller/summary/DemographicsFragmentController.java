package org.openmrs.module.registrationapp.fragment.controller.summary;


import org.openmrs.Patient;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.layout.web.name.NameTemplate;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

import java.util.List;

public class DemographicsFragmentController {
    public void controller(FragmentConfiguration config
            , @InjectBeans PatientDomainWrapper patientWrapper
            , @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate) {

        config.require("patient");
        Object patient = config.get("patient");

        if (patient instanceof Patient) {
            patientWrapper.setPatient((Patient) patient);
            config.addAttribute("patient", patientWrapper);
        } else if (patient instanceof PatientDomainWrapper) {
            patientWrapper = (PatientDomainWrapper) patient;
        }

        config.addAttribute("nameTemplate", nameTemplate);

    }
}
