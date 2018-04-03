package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationapp.PropertiesUtil;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class M2sysEnrollOrUpdateFragmentController {

    public void controller(FragmentModel model, @FragmentParam("app") AppDescriptor app,
            @FragmentParam("patientId") Integer patientId) {

        Patient patient = Context.getPatientService().getPatient(patientId);
        String biometricID = "";
        String enrollOrUpdate = "";

        PatientIdentifierType biometricPatientIdentifierType = PropertiesUtil.getLocalFpType();

        PatientIdentifier biometricIdentifier = patient.getPatientIdentifier(biometricPatientIdentifierType);

        if (biometricIdentifier == null) {
            enrollOrUpdate = "enroll";
        } else {
            biometricID = biometricIdentifier.getIdentifier();
            enrollOrUpdate = "update";
        }

        model.addAttribute("biometricPatientIdentifierType", biometricPatientIdentifierType);
        model.addAttribute("biometricID", biometricID);
        model.addAttribute("enrollOrUpdate", enrollOrUpdate);
    }
}
