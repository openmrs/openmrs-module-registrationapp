package org.openmrs.module.registrationapp.fragment.controller.summary;


import org.openmrs.Patient;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

public class ContactInfoFragmentController {
    public void controller(FragmentConfiguration config
            , @InjectBeans PatientDomainWrapper patientWrapper) {

        config.require("patient");
        Object patient = config.get("patient");

        if (patient instanceof Patient) {
            patientWrapper.setPatient((Patient) patient);
            config.addAttribute("patient", patientWrapper);
        } else if (patient instanceof PatientDomainWrapper) {
            patientWrapper = (PatientDomainWrapper) patient;
        }

    }
}
