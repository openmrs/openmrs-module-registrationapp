package org.openmrs.module.registrationapp.action;

import org.openmrs.Patient;

import java.util.Map;

/**
 * An action that can be configured to run after creating a patient, for a particular instance of the registration app
 */
public interface AfterPatientCreatedAction {


    /**
     * @param created the patient who is being created
     * @param submittedParameters parameters, typically from an HttpServletRequest
     */
    void afterPatientCreated(Patient created, Map<String, String[]> submittedParameters);

}
