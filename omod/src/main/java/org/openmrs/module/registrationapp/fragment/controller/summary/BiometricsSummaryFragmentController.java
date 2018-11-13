package org.openmrs.module.registrationapp.fragment.controller.summary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiometricsSummaryFragmentController {

    protected final Log log = LogFactory.getLog(this.getClass());

    public void controller(FragmentConfiguration config,
                           FragmentModel model,
                           @SpringBean RegistrationCoreProperties registrationCoreProperties,
                           @SpringBean("patientService") PatientService patientService,
                           @SpringBean AppFrameworkService appFrameworkService,
                           @RequestParam("patientId") Patient patient) throws Exception {

        // TODO handle translating the rest of the fingerprint types?
        // TODO handle permissioning

        model.put("status", "");
        model.put("identifierToSubjectMap", null);

        AppDescriptor app = appFrameworkService.getApp((String) config.get("app"));
        String registrationAppId = app.getConfig().get("registrationAppId").getTextValue();
        model.put("registrationAppId", registrationAppId);
        AppDescriptor registrationApp = appFrameworkService.getApp(registrationAppId);
        NavigableFormStructure form = RegisterPatientFormBuilder.buildFormStructure(registrationApp);

        BiometricEngine engine = registrationCoreProperties.getBiometricEngine();
        BiometricEngineStatus status =  engine.getStatus();
        if (!status.isEnabled()) {
            model.put("status", "registrationapp.biometrics.biometricsServiceNotEnabled");
            return;  // don't bother going any further
        }

        List<PatientIdentifierType> biometricIdentifierTypes = new ArrayList<PatientIdentifierType>();

        // get all possible biometric identifier types
        for (String uuid : RegisterPatientFormBuilder.extractBiometricIdentifierTypes(form)) {
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
                BiometricSubject subject = null;
                try {
                    subject = engine.lookup(identifier.getIdentifier());
                }
                catch (HttpClientErrorException e) {
                    // ignore errors (specifically 404 which will be returns if no matching biometric is found)
                }
                catch (ResourceAccessException e) {
                    log.error("Unable to connect to biometrics server", e);
                }
                if (subject != null) {
                    identifierToSubjectMap.put(identifier, subject);
                }
            }

        }
        catch (HttpServerErrorException e) {
            model.put("status", "registrationapp.biometrics.errorContactingBiometricsServer");
            return;  // don't go any further
        }

        model.put("identifierToSubjectMap", identifierToSubjectMap);
    }

}
