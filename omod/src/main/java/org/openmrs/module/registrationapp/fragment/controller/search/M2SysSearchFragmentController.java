package org.openmrs.module.registrationapp.fragment.controller.search;

import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.module.m2sysbiometrics.model.TempFingerprint;
import org.openmrs.module.m2sysbiometrics.service.TempFingerprintService;
import org.openmrs.module.registrationapp.PropertiesUtil;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.biometrics.model.EnrollmentResult;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

public class M2SysSearchFragmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(M2SysSearchFragmentController.class);

    public void controller(FragmentModel model) {
        model.addAttribute("test", "testval");
    }

    public List<SimpleObject> getPatients(@SpringBean("registrationCoreService") RegistrationCoreService registrationCoreService,
                                          UiUtils ui) {
        List<PatientAndMatchQuality> patients = registrationCoreService.findByBiometricMatch(new BiometricSubject());

        return simplify(ui, patients);
    }

    public FragmentActionResult importMpiPatient(@RequestParam("nationalFingerprintId") String nationalId,
            @SpringBean("registrationCoreService") RegistrationCoreService registrationService,
            @SpringBean("tempFingerprintService") TempFingerprintService tempFingerprintService,
            HttpSession session) {
        FragmentActionResult result;
        try {
            TempFingerprint fingerprint = tempFingerprintService.findOneByBiometricId(nationalId);
            EnrollmentResult enrollmentResult = registrationService.getBiometricEngine()
                    .enroll(fingerprint.getBiometricXml());

            Patient registeredPatient = registrationService.findByPatientIdentifier(
                    enrollmentResult.getLocalBiometricSubject().getSubjectId(),
                    PropertiesUtil.getLocalFpType().getUuid());
            result = new SuccessResult(registeredPatient.getUuid());
        } catch (Exception ex) {
            String message = "Error during importing patient by national fingerprint id. Details: " + ex.getMessage();
            LOGGER.error(message, ex);
            result = new FailureResult(message);
        }
        return result;
    }

    private List<SimpleObject> simplify(UiUtils ui, List<PatientAndMatchQuality> results) {
        List<SimpleObject> patients = new ArrayList<SimpleObject>(results.size());
        for (PatientAndMatchQuality patient : results) {
            patients.add(simplify(ui, patient));
        }
        return patients;
    }

    private SimpleObject simplify(UiUtils ui, PatientAndMatchQuality patientAndMatchQuality) {
        Patient patient = patientAndMatchQuality.getPatient();
        PersonName name = patient.getPersonName();
        SimpleObject preferredName = SimpleObject.fromObject(name, ui, "givenName", "middleName",
                "familyName", "familyName2");
        preferredName.put("display", ui.format(name));

        SimpleObject personObj = SimpleObject.fromObject(patient, ui, "patientId", "gender", "age",
                "birthdate", "birthdateEstimated");
        personObj.put("personName", preferredName);

        PatientIdentifier primaryIdentifier = patient.getPatientIdentifier();
        PatientIdentifier nationalFpIdentifier = getNationalFpId(patient);

        SimpleObject patientObj = SimpleObject.fromObject(patient, ui, "patientId", "uuid");

        patientObj.put("person", personObj);
        patientObj.put("patientIdentifier", prepareSimpleObjectForPatientId(primaryIdentifier, ui));
        patientObj.put("nationalFingerprintPatientIdentifier", prepareSimpleObjectForPatientId(nationalFpIdentifier, ui));
        patientObj.put("onlyInMpi", isPatientOnlyInMpi(patientAndMatchQuality));

        return patientObj;
    }

    private boolean isPatientOnlyInMpi(PatientAndMatchQuality patient) {
        boolean isNational = false;
        boolean isLocal = false;
        for (String field : patient.getMatchedFields()) {
            if (StringUtils.equals(field, RegistrationCoreConstants.NATIONAL_FINGERPRINT_NAME)) {
                isNational = true;
            }
            if (StringUtils.equals(field, RegistrationCoreConstants.LOCAL_FINGERPRINT_NAME)) {
                isLocal = true;
            }
        }
        return !isLocal && isNational;
    }

    private PatientIdentifier getNationalFpId(Patient patient) {
        PatientIdentifier nationalFpIdentifier = null;
        if (PropertiesUtil.nationalFpTypeSet()) {
            PatientIdentifierType nationalFpPit = PropertiesUtil.getNationalFpType();
            nationalFpIdentifier = patient.getPatientIdentifier(nationalFpPit);
        }
        return nationalFpIdentifier;
    }

    private SimpleObject prepareSimpleObjectForPatientId(PatientIdentifier patientIdentifier, UiUtils ui) {
        SimpleObject simpleObject = null;
        if (patientIdentifier != null) {
            simpleObject =  SimpleObject.fromObject(patientIdentifier, ui, "uuid", "identifier");
        }
        return simpleObject;
    }
}
