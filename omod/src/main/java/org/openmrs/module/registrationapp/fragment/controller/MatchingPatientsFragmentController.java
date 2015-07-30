/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationapp.fragment.controller;

import org.openmrs.Patient;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MatchingPatientsFragmentController {

    public static final int MAX_RESULTS = 10;
    public static final double CUTOFF = 2.0;

    public static final String[] PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "patientIdentifier.preferred", "patientIdentifier.identifier", "gender", "birthdate", "personAddress"};

    public static final String[] MPI_PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "patientIdentifier.preferred", "patientIdentifier.identifier", "gender", "birthdate", "personAddress", "mpiPatient"};

    public List<SimpleObject> getSimilarPatients(@RequestParam("appId") AppDescriptor app,
	                                             @SpringBean("registrationCoreService") RegistrationCoreService service,
	                                             @ModelAttribute("patient") @BindParams Patient patient,
	                                             @ModelAttribute("personName") @BindParams PersonName name,
	                                             @ModelAttribute("personAddress") @BindParams PersonAddress address,
	                                             HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);
        List<PatientAndMatchQuality> matches = service.findFastSimilarPatients(patient, null, CUTOFF, MAX_RESULTS);
        return getSimpleObjects(ui, matches);
    }

    public List<SimpleObject> getExactPatients(@RequestParam("appId") AppDescriptor app,
                                               @SpringBean("registrationCoreService") RegistrationCoreService service,
                                               @ModelAttribute("patient") @BindParams Patient patient,
                                               @ModelAttribute("personName") @BindParams PersonName name,
                                               @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                               HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);
        List<PatientAndMatchQuality> matches = service.findPreciseSimilarPatients(patient, null, CUTOFF, MAX_RESULTS);
        return getSimpleObjects(ui, matches);
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
            if (patientEntry instanceof MpiPatient) {
                result.add(SimpleObject.fromObject(patientEntry, ui, MPI_PATIENT_PROPERTIES));
            } else {
                result.add(SimpleObject.fromObject(patientEntry, ui, PATIENT_PROPERTIES));
            }
        }
        return result;
    }
}
