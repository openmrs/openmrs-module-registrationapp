package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class M2sysEnrollOrUpdateFragmentController {

	private PatientService patientService;

	public void controller(FragmentModel model, @FragmentParam("app") AppDescriptor app, 
			 @FragmentParam("patientId") Integer patientId) {

		patientService =  Context.getService(PatientService.class);
		Patient patient = patientService.getPatient(patientId);
		String biometricID = "";
		String enrollOrUpdate = "";
		
		PatientIdentifierType biometricPatientIdentifierType = patientService.getPatientIdentifierTypeByUuid(Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID));
        if (biometricPatientIdentifierType == null) {
            throw new APIException("Local fingerprint identifier type not found. Make sure the registrationcore.biometrics.personIdentifierTypeUuid global property is set.");
        }
        
        PatientIdentifier biometricIdentifier = patient.getPatientIdentifier(biometricPatientIdentifierType);
        
        if (biometricIdentifier == null ){
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