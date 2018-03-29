package org.openmrs.module.registrationapp.fragment.controller.search;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationapp.PropertiesUtil;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;

public class M2SysSearchFragmentController {

    public void controller(FragmentModel model) {
        model.addAttribute("test", "testval");
    }

    public List<SimpleObject> getPatients(@SpringBean("registrationCoreService") RegistrationCoreService registrationCoreService,
                                          UiUtils ui) {
        List<PatientAndMatchQuality> matches = registrationCoreService.findByBiometricMatch(new BiometricSubject());

        List<Patient> patients = toPatientList(matches);
        return simplify(ui, patients);
    }

    private List<Patient> toPatientList(List<PatientAndMatchQuality> matches) {
        List<Patient> patients = new ArrayList<Patient>();

        for (PatientAndMatchQuality match : matches) {
            patients.add(match.getPatient());
        }

        return patients;
    }

    private List<SimpleObject> simplify(UiUtils ui, List<Patient> results) {
        List<SimpleObject> patients = new ArrayList<SimpleObject>(results.size());
        for (Patient patient : results) {
            patients.add(simplify(ui, patient));
        }
        return patients;
    }

    private SimpleObject simplify(UiUtils ui, Patient patient) {
        PersonName name = patient.getPersonName();
        SimpleObject preferredName = SimpleObject.fromObject(name, ui, "givenName", "middleName",
                "familyName", "familyName2");
        preferredName.put("display", ui.format(name));

        SimpleObject personObj = SimpleObject.fromObject(patient, ui, "patientId", "gender", "age",
                "birthdate", "birthdateEstimated");
        personObj.put("personName", preferredName);

        PatientIdentifier primaryIdentifier = patient.getPatientIdentifier();
        PatientIdentifier localFpIdentifier = getLocalFpId(patient);
        PatientIdentifier nationalFpIdentifier = getNationalFpId(patient);

        SimpleObject patientObj = SimpleObject.fromObject(patient, ui, "patientId", "uuid");

        patientObj.put("person", personObj);
        patientObj.put("patientIdentifier", prepareSimpleObjectForPatientId(primaryIdentifier, ui));
        patientObj.put("localFingerprintPatientIdentifier", prepareSimpleObjectForPatientId(localFpIdentifier, ui));
        patientObj.put("nationalFingerprintPatientIdentifier", prepareSimpleObjectForPatientId(nationalFpIdentifier, ui));

        return patientObj;
    }

    private PatientIdentifier getNationalFpId(Patient patient) {
        PatientIdentifier nationalFpIdentifier = null;
        if (PropertiesUtil.nationalFpTypeUuidSet()) {
            PatientIdentifierType nationalFpPit = Context.getPatientService()
                    .getPatientIdentifierTypeByUuid(PropertiesUtil.getNationalFpTypeUuid());
            nationalFpIdentifier = patient.getPatientIdentifier(nationalFpPit);
        }
        return nationalFpIdentifier;
    }

    private PatientIdentifier getLocalFpId(Patient patient) {
        PatientIdentifier localFpIdentifier = null;
        if (PropertiesUtil.localFpTypeUuidSet()) {
            PatientIdentifierType localFpPit = Context.getPatientService()
                    .getPatientIdentifierTypeByUuid(PropertiesUtil.getLocalFpTypeUuid());
            localFpIdentifier = patient.getPatientIdentifier(localFpPit);
        }
        return localFpIdentifier;
    }

    private SimpleObject prepareSimpleObjectForPatientId(PatientIdentifier patientIdentifier, UiUtils ui) {
        SimpleObject simpleObject = null;
        if (patientIdentifier != null) {
            simpleObject =  SimpleObject.fromObject(patientIdentifier, ui, "uuid", "identifier");
        }
        return simpleObject;
    }
}
