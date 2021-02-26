package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationapp.PropertiesUtil;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class M2sysEnrollOrUpdateFragmentController {
    private AdministrationService adminService;

    public M2sysEnrollOrUpdateFragmentController() {
        adminService = Context.getAdministrationService();
    }

    public void controller(FragmentModel model, @FragmentParam("app") AppDescriptor app,
                           @FragmentParam("patientId") Integer patientId) {

        Patient patient = Context.getPatientService().getPatient(patientId);
        String localBiometricID = "", nationalBiometricID = "";
        String enrollOrUpdate = "";

        PatientIdentifierType localBiometricPatientIdentifierType = PropertiesUtil.getLocalFpType();
        PatientIdentifierType nationalBiometricPatientIdentifierType = PropertiesUtil.getNationalFpType();

        PatientIdentifier localBiometricIdentifier = patient.getPatientIdentifier(localBiometricPatientIdentifierType);
        PatientIdentifier nationalBiometricIdentifier = patient.getPatientIdentifier(nationalBiometricPatientIdentifierType);

        if (localBiometricIdentifier == null && nationalBiometricIdentifier == null) {
            enrollOrUpdate = "enroll";
        } else {
            localBiometricID = localBiometricIdentifier != null ? localBiometricIdentifier.getIdentifier() : "";
            nationalBiometricID = nationalBiometricIdentifier != null ? nationalBiometricIdentifier.getIdentifier() : "";
            enrollOrUpdate = "update";
        }

        model.addAttribute("biometricPatientIdentifierType", localBiometricPatientIdentifierType);
        model.addAttribute("nationalBiometricPatientIdentifierType", nationalBiometricPatientIdentifierType);
        model.addAttribute("biometricID", localBiometricID);
        model.addAttribute("nationalBiometricID", nationalBiometricID);
        model.addAttribute("enrollOrUpdate", enrollOrUpdate);

        RegistrationAppUiUtils.fetchBiometricConstants(model, adminService);


    }
}
