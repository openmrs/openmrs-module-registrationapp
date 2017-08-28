package org.openmrs.module.registrationapp.page.controller.biometrics;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditBiometricsPageController {

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("patientId") Patient patient,
                    @RequestParam("registrationAppId") AppDescriptor registrationApp,
                    @SpringBean RegistrationCoreProperties registrationCoreProperties,
                    @SpringBean("patientService") PatientService patientService) throws Exception {

        // TODO: handle returnUrl? right now assumption is that it comes from the registraton summary page

        model.put("identifierToSubjectMap", null);
        model.put("status", "");
        model.put("registrationAppId", registrationApp.getId());

        sessionContext.requireAuthentication();

        NavigableFormStructure form = RegisterPatientFormBuilder.buildFormStructure(registrationApp);

        BiometricEngine engine = registrationCoreProperties.getBiometricEngine();
        BiometricEngineStatus status =  engine.getStatus();
        if (!status.isEnabled()) {
            model.put("status", "registrationapp.biometrics.biometricsServiceNotEnabled");
            model.put("identifierToSubjectMap", null);
            return;  // don't go any further
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
                    // ignore errors (specifically 404 which will be returns if no matching biometric is found
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
