package org.openmrs.module.registrationapp.fragment.controller.summary;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiometricsSummaryFragmentController {

    public void controller(FragmentConfiguration config,
                           FragmentModel model,
                           @SpringBean RegistrationCoreProperties registrationCoreProperties,
                           @SpringBean("patientService") PatientService patientService,
                           @SpringBean AppFrameworkService appFrameworkService,
                           @RequestParam("patientId") Patient patient) throws Exception {

        // TODO handle translating the rest of the fingerprint types?
        // TODO handle permissioning

        BiometricEngine engine = registrationCoreProperties.getBiometricEngine();
        BiometricEngineStatus status =  engine.getStatus();
        if (!status.isEnabled()) {
            model.put("status", "registrationapp.biometrics.biometricsServiceNotEnabled");
            model.put("identifierToSubjectMap", null);
            return;  // don't go any further
        }


        AppDescriptor app = appFrameworkService.getApp((String) config.get("app"));
        AppDescriptor registrationApp = appFrameworkService.getApp(app.getConfig().get("registrationAppId").getTextValue());
        NavigableFormStructure form = RegisterPatientFormBuilder.buildFormStructure(registrationApp);

        List<PatientIdentifierType> biometricIdentifierTypes = new ArrayList<PatientIdentifierType>();

        // get all possible biometric identifier types
        for (String uuid : extractBiometricIdentifierTypes(form)) {
            biometricIdentifierTypes.add(patientService.getPatientIdentifierTypeByUuid(uuid));
        }


        // now get all biometric identifiers for this patient
        List<PatientIdentifier> biometricIdentifiers = new ArrayList<PatientIdentifier>();

        for (PatientIdentifierType type : biometricIdentifierTypes) {
            biometricIdentifiers.addAll(patient.getPatientIdentifiers(type));
        }

         engine = registrationCoreProperties.getBiometricEngine();

        Map<PatientIdentifier, BiometricSubject> identifierToSubjectMap = new HashMap<PatientIdentifier, BiometricSubject>();

        try {

            for (PatientIdentifier identifier : biometricIdentifiers) {
                BiometricSubject subject = engine.lookup(identifier.getIdentifier());
                if (subject != null) {
                    identifierToSubjectMap.put(identifier, subject);
                }
            }

        }
        catch (HttpServerErrorException e) {
            model.put("status", "registrationapp.biometrics.errorContactingBiometricsServer");
            model.put("identifierToSubjectMap", null);
            return;  // don't go any further
        }

        model.put("status", "");
        model.put("identifierToSubjectMap", identifierToSubjectMap);
    }


    // move to RegistrationFormBuilder or some other central location
    public List<String> extractBiometricIdentifierTypes(NavigableFormStructure form) {

        List<String> biometricIdentifierUuids = new ArrayList<String>();

        List<Field> fields = form.getFields();
        if (fields != null) {
            for (Field field : fields) {
                if (StringUtils.equals(field.getType(), "fingerprint")) {
                     biometricIdentifierUuids.add(field.getUuid());
                }
            }
        }

        return biometricIdentifierUuids;
    }
}
