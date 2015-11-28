/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationapp.fragment.controller;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class MatchingPatientsFragmentController {

    public static final int MAX_RESULTS = 10;
    public static final double CUTOFF = 2.0;

    public static final String[] PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "gender", "birthdate", "personAddress"};

    public static final String[] MPI_PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "gender", "birthdate", "personAddress", "mpiPatient"};

    public List<SimpleObject> getSimilarPatients(@RequestParam("appId") AppDescriptor app,
                                                 @SpringBean("registrationCoreService") RegistrationCoreService service,
                                                 @ModelAttribute("patient") @BindParams Patient patient,
                                                 @ModelAttribute("personName") @BindParams PersonName name,
                                                 @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                                 @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                                                 @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                                                 HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);

        Map<String, Object> otherDataPoints = createDataPoints(birthdateYears, birthdateMonths);

        List<PatientAndMatchQuality> matches = service.findFastSimilarPatients(patient, otherDataPoints, CUTOFF, MAX_RESULTS);
        return getSimpleObjects(ui, matches);
    }

    public List<SimpleObject> getExactPatients(@RequestParam("appId") AppDescriptor app,
                                               @SpringBean("registrationCoreService") RegistrationCoreService service,
                                               @ModelAttribute("patient") @BindParams Patient patient,
                                               @ModelAttribute("personName") @BindParams PersonName name,
                                               @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                               @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                                               @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                                               HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);

        Map<String, Object> otherDataPoints = createDataPoints(birthdateYears, birthdateMonths);

        List<PatientAndMatchQuality> matches = service.findPreciseSimilarPatients(patient, otherDataPoints, CUTOFF, MAX_RESULTS);
        return getSimpleObjects(ui, matches);
    }

    private Map<String, Object> createDataPoints(Integer birthdateYears, Integer birthdateMonths) {
        Map<String, Object> otherDataPoints = new HashMap<String, Object>();
        otherDataPoints.put("birthdateYears", birthdateYears);
        otherDataPoints.put("birthdateMonths", birthdateMonths);
        return otherDataPoints;
    }

    private void addToPatient(Patient patient, AppDescriptor app, PersonName name, PersonAddress address, HttpServletRequest request) throws IOException {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        patient.addName(name);
        patient.addAddress(address);

        if (formStructure != null) {
            RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
        }
    }

    private List<SimpleObject> getSimpleObjects(UiUtils ui, List<PatientAndMatchQuality> matches) {
        List<SimpleObject> result = new ArrayList<SimpleObject>();

        for (PatientAndMatchQuality matchedPatient : matches) {
            Patient patientEntry = matchedPatient.getPatient();
            SimpleObject patientSimple;
            if (patientEntry instanceof MpiPatient) {
                patientSimple = SimpleObject.fromObject(patientEntry, ui, MPI_PATIENT_PROPERTIES);
            } else {
                patientSimple = SimpleObject.fromObject(patientEntry, ui, PATIENT_PROPERTIES);
            }
            addIdentifiersToPatientSimple(patientEntry, patientSimple);
            result.add(patientSimple);
        }
        return result;
    }

    private void addIdentifiersToPatientSimple(Patient patientEntry, SimpleObject patientSimple) {
        LinkedList<SimpleObject> identifiersList = new LinkedList<SimpleObject>();
        for (PatientIdentifier identifier : patientEntry.getIdentifiers()) {
            SimpleObject identifierEntry = new SimpleObject();
            identifierEntry.put("name", identifier.getIdentifierType().getName());
            identifierEntry.put("value", identifier.getIdentifier());
            if (identifier.isPreferred()) {
                identifiersList.addFirst(identifierEntry);
            } else {
                identifiersList.add(identifierEntry);
            }
        }
        patientSimple.put("identifiers", identifiersList);
    }
}
