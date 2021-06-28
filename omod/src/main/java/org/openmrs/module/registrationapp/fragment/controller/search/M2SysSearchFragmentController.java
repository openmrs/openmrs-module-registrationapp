package org.openmrs.module.registrationapp.fragment.controller.search;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.m2sysbiometrics.model.TempFingerprint;
import org.openmrs.module.m2sysbiometrics.service.TempFingerprintService;
import org.openmrs.module.registrationapp.PropertiesUtil;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.biometrics.model.EnrollmentResult;
import org.openmrs.module.registrationcore.api.biometrics.model.EnrollmentStatus;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

public class M2SysSearchFragmentController {
    private BiometricEngine biometricEngine;
    private RegistrationCoreService registrationCoreService;
    private PatientService patientService;
    private AdministrationService adminService;

    private static final Logger LOGGER = LoggerFactory.getLogger(M2SysSearchFragmentController.class);
    private final Log log = LogFactory.getLog(M2SysSearchFragmentController.class);

    public M2SysSearchFragmentController() {
        adminService = Context.getAdministrationService();
        registrationCoreService = Context.getService(RegistrationCoreService.class);
        biometricEngine = registrationCoreService.getBiometricEngine();
        patientService = Context.getService(PatientService.class);
    }

    public void controller(FragmentModel model) {
        model.addAttribute("test", "testval");
        RegistrationAppUiUtils.fetchBiometricConstants(model, adminService);
    }

    public List<SimpleObject> getPatients(@SpringBean("registrationCoreService") RegistrationCoreService registrationCoreService,
                                          UiUtils ui) {
        List<PatientAndMatchQuality> patients = registrationCoreService.findByBiometricMatch(new BiometricSubject());

        return simplify(ui, patients);
    }

    private boolean isBiometricEngineEnabled() {
        return getBiometricEngine() != null;
    }

    private BiometricEngine getBiometricEngine() {
        return biometricEngine;
    }

    public SimpleObject search(@SpringBean("messageSourceService") MessageSourceService messageSourceService,
                               @RequestParam("biometricXml") String biometricXml,
                               @SpringBean("registrationCoreService") RegistrationCoreService registrationService) {
        SimpleObject response = new SimpleObject();
        if (!isBiometricEngineEnabled()) {
            response.put("success", false);
            response.put("message", messageSourceService.getMessage("registrationapp.biometrics.m2sys.errorEngine"));
            return response;
        }

        try {
            EnrollmentResult result = biometricEngine.enroll(biometricXml);
            response.put("success", true);
            response.put("localBiometricSubjectId", result.getLocalBiometricSubject().getSubjectId());
            response.put("nationalBiometricSubjectId", result.getNationalBiometricSubject().getSubjectId());
            response.put("status", result.getEnrollmentStatus().name());

            if (result.getEnrollmentStatus() == EnrollmentStatus.ALREADY_REGISTERED) {
//                Check and load patient
                Patient patient = null;
                if (StringUtils.isNotBlank(result.getNationalBiometricSubject().getSubjectId()) && StringUtils.isNotBlank(result.getLocalBiometricSubject().getSubjectId())) {
                    log.info("Matches Found on Both the Local and National FP Servers");
//                    Search by local FP id first, then national
                    patient = findByFingerprintId(result.getLocalBiometricSubject().getSubjectId(), PropertiesUtil.getLocalFpType());
                    if (patient == null) {
                        patient = findByFingerprintId(result.getNationalBiometricSubject().getSubjectId(), PropertiesUtil.getNationalFpType());
                    }
                } else if (StringUtils.isNotBlank(result.getNationalBiometricSubject().getSubjectId())){
                    log.info("Match Found on National FP Server");
                    patient = findByFingerprintId(result.getNationalBiometricSubject().getSubjectId(), PropertiesUtil.getNationalFpType());
                } else if (StringUtils.isNotBlank(result.getLocalBiometricSubject().getSubjectId())) {
                    log.info("Match Found on Local FP");
                    patient = findByFingerprintId(result.getLocalBiometricSubject().getSubjectId(), PropertiesUtil.getLocalFpType());
                }

                if (patient != null) {
                    response.put("patientName", patient.getPersonName().getFullName());
                    response.put("patientDob", new SimpleDateFormat("dd-MM-yyyy").format(patient.getBirthdate()));
                    response.put("patientGender", patient.getGender().equals("M") ? "Male" : "Female");
                    if (patient.getAttribute("Telephone Number") != null) {
                        response.put("phoneNumber", patient.getAttribute("Telephone Number").getValue());
                    }
                    if (patient.getAttribute("Health Center") != null) {
                        response.put("sourceLocation", patient.getAttribute("Health Center").getValue());
                    }
                    if (patient.getAttribute("First Name of Mother") != null) {
                        response.put("mothersName", patient.getAttribute("First Name of Mother").getValue());
                    }
                    response.put("personAddress", patient.getPersonAddress().getAddress1() + ", " +
                            patient.getPersonAddress().getCityVillage() + ", " +
                            patient.getPersonAddress().getStateProvince() + ", " +
                            patient.getPersonAddress().getCountry());
                    StringBuffer identifierString = new StringBuffer();
                    for (PatientIdentifier pId : patient.getIdentifiers()) {
                        if (pId.getIdentifierType().equals(PropertiesUtil.getCodeNationalIdType()) ||
                                pId.getIdentifierType().equals(PropertiesUtil.getCodePcIdType()) ||
                                pId.getIdentifierType().equals(PropertiesUtil.getCodeStIdType()) ||
                                pId.getIdentifierType().equals(PropertiesUtil.getIsantePlusIdType())) {
                            identifierString.append(pId.getIdentifierType().getName() + ": " + pId.getIdentifier() + ", \n");
                        }
                    }
                    response.put("patientIdentifiers", identifierString);
                    response.put("patientUuid", patient.getUuid());
                } else {
                    log.info("No patient found with the used  fingerprint IDs ");
                }
            }

        } catch (Exception ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());

            LOGGER.error("Fingerprints enrollment failed", ex);
            LOGGER.error(ExceptionUtils.getFullStackTrace(ex));
        }

        return response;
    }

    private Patient findByFingerprintId(String subjectId, PatientIdentifierType identifierType) {
        Patient patient = registrationCoreService.findByPatientIdentifier(subjectId, identifierType.getUuid());
        if (patient == null) {
            LOGGER.error("Patient with local fingerprint ID " + subjectId + " doesn't exist: ");
        }
        return patient;
    }

    public FragmentActionResult importMpiPatientWithCcd(@RequestParam("nationalFingerprintId") String nationalId,
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
            if (registrationService.importCcd(registeredPatient) == null) {
                LOGGER.error("Ccd import failure");
            }
            result = new SuccessResult(registeredPatient.getUuid());
        } catch (Exception ex) {
            String message = "Error during importing patient with ccd by national fingerprint id. Details: " + ex
                    .getMessage();
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
                "birthdateEstimated");
        personObj.put("personName", preferredName);
        personObj.put("birthdate", formatDate(patient.getBirthdate()));

        PatientIdentifier primaryIdentifier = patient.getPatientIdentifier(PropertiesUtil.getIsantePlusIdType());
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
            simpleObject = SimpleObject.fromObject(patientIdentifier, ui, "uuid", "identifier");
        }
        return simpleObject;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(date);
    }
}
