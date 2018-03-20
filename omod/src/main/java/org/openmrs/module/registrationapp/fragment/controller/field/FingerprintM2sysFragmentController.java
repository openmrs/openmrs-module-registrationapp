package org.openmrs.module.registrationapp.fragment.controller.field;

import static org.openmrs.module.registrationcore.RegistrationCoreConstants.GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.biometrics.model.EnrollmentResult;
import org.openmrs.module.registrationcore.api.biometrics.model.EnrollmentStatus;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

public class FingerprintM2sysFragmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FingerprintM2sysFragmentController.class);

    private BiometricEngine biometricEngine;

    private AdministrationService adminService;

    private RegistrationCoreService registrationCoreService;

    public FingerprintM2sysFragmentController() {
        adminService = Context.getAdministrationService();
        registrationCoreService = Context.getService(RegistrationCoreService.class);
        biometricEngine = registrationCoreService.getBiometricEngine();
    }

    public void controller() {
    }

    public SimpleObject enroll(@SpringBean("messageSourceService") MessageSourceService messageSourceService,
            @SpringBean RegistrationCoreService registrationCoreService) {
        SimpleObject response = new SimpleObject();
        if (!isBiometricEngineEnabled()) {
            response.put("success", false);
            response.put("message", messageSourceService.getMessage("registrationapp.biometrics.m2sys.errorEngine"));
            return response;
        }

        try {
            EnrollmentResult result = biometricEngine.enroll(null);
            response.put("success", true);
            response.put("localBiometricSubjectId", result.getLocalBiometricSubject().getSubjectId());
            response.put("nationalBiometricSubjectId", result.getNationalBiometricSubject().getSubjectId());
            response.put("status", result.getEnrollmentStatus().name());
            if (result.getEnrollmentStatus() == EnrollmentStatus.ALREADY_REGISTERED) {
                Patient patient = findByLocalFpId(result.getLocalBiometricSubject().getSubjectId());
                response.put("patientUuid", patient.getUuid());
            }
        } catch (Exception ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            LOGGER.error(ex.getMessage());
        }

        return response;
    }

    public SimpleObject getStatus() {
        if (!isBiometricEngineEnabled()) {
            return null;
        }
        BiometricEngineStatus response = biometricEngine.getStatus();

        SimpleObject result = new SimpleObject();
        result.put("enabled", response.isEnabled());
        result.put("statusMessage", response.getStatusMessage());
        result.put("description", response.getDescription());

        return result;
    }

    public SimpleObject update(@RequestParam("id") String id,
            @SpringBean("messageSourceService") MessageSourceService messageSourceService) {
        SimpleObject response = new SimpleObject();
        if (!isBiometricEngineEnabled()) {
            response.put("success", false);
            response.put("message", messageSourceService.getMessage("registrationapp.biometrics.m2sys.errorEngine"));
            return response;
        }

        try {
            BiometricSubject biometricSubject = new BiometricSubject();
            biometricSubject.setSubjectId(id);
            BiometricSubject result = biometricEngine.update(biometricSubject);
            response.put("success", true);
            response.put("message", result.getSubjectId());
        } catch (Exception ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            LOGGER.error(ex.getMessage());
        }
        return response;
    }

    public SimpleObject updateSubjectId(@RequestParam("oldId") String oldId,
            @RequestParam("newId") String newId) {
        if (!isBiometricEngineEnabled()) {
            return null;
        }
        BiometricSubject response = biometricEngine.updateSubjectId(oldId, newId);

        SimpleObject result = new SimpleObject();
        result.put("id", response.getSubjectId());
        return result;
    }

    public List<SimpleObject> search(@RequestParam("id") String id) {
        if (!isBiometricEngineEnabled()) {
            return null;
        }
        BiometricSubject biometricSubject = new BiometricSubject();
        biometricSubject.setSubjectId(id);
        List<BiometricMatch> response = biometricEngine.search(biometricSubject);

        return toSimpleObjectList(response);
    }

    public SimpleObject lookup(@RequestParam("id") String id) {
        if (!isBiometricEngineEnabled()) {
            return null;
        }
        BiometricSubject response = biometricEngine.lookup(id);

        SimpleObject result = new SimpleObject();
        result.put("id", response.getSubjectId());
        return result;
    }

    public void delete(@RequestParam("id") String id) {
        if (!isBiometricEngineEnabled()) {
            return;
        }
        biometricEngine.delete(id);
    }

    private List<SimpleObject> toSimpleObjectList(List<BiometricMatch> matches) {
        List<SimpleObject> resultList = new ArrayList<SimpleObject>();
        for (BiometricMatch match : matches) {
            SimpleObject simpleObject = new SimpleObject();
            simpleObject.put("subjectId", match.getSubjectId());
            simpleObject.put("matchScore", match.getMatchScore());
            resultList.add(simpleObject);
        }
        return resultList;
    }

    private boolean isBiometricEngineEnabled() {
        return getBiometricEngine() != null;
    }

    public BiometricEngine getBiometricEngine() {
        return biometricEngine;
    }

    private Patient findByLocalFpId(String subjectId) {
        String identifierUuid = getGlobalProperty(GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID);
        Patient patient = registrationCoreService.findByPatientIdentifier(subjectId, identifierUuid);
        if (patient == null) {
            throw new APIException(String.format("Patient with local fingerprint UUID %s doesn't exist", subjectId));
        }
        return patient;
    }

    private String getGlobalProperty(String propertyName) {
        String propertyValue = adminService.getGlobalProperty(propertyName);
        if (StringUtils.isBlank(propertyValue)) {
            throw new APIException(String.format("Property value for '%s' is not set", propertyName));
        }
        return propertyValue;
    }
}
