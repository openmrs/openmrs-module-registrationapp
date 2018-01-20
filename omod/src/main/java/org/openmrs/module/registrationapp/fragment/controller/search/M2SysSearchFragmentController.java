package org.openmrs.module.registrationapp.fragment.controller.search;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
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

    List<SimpleObject> simplify(UiUtils ui, List<Patient> results) {
        List<SimpleObject> patients = new ArrayList<SimpleObject>(results.size());
        for (Patient patient : results) {
            patients.add(simplify(ui, patient));
        }
        return patients;
    }

    SimpleObject simplify(UiUtils ui, Patient patient) {
        PersonName name = patient.getPersonName();
        SimpleObject preferredName = SimpleObject.fromObject(name, ui, "givenName", "middleName", "familyName",
                "familyName2");
        preferredName.put("display", ui.format(name));

        PatientIdentifier primaryIdentifier = patient.getPatientIdentifier();
        SimpleObject identifierObj = SimpleObject.fromObject(primaryIdentifier, ui, "uuid", "identifier");

        SimpleObject personObj = SimpleObject.fromObject(patient, ui, "patientId", "gender", "age", "birthdate",
                "birthdateEstimated");
        personObj.put("personName", preferredName);

        SimpleObject patientObj = SimpleObject.fromObject(patient, ui, "patientId", "uuid");
        patientObj.put("patientIdentifier", identifierObj);
        patientObj.put("person", personObj);

        return patientObj;
    }
}
