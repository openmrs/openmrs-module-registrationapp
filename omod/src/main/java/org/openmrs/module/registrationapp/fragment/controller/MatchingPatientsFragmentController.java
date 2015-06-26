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
import org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatient;
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

    public static final String[] PATIENT_PROPERTIES = new String[]{"patientId", "givenName", "familyName",
            "patientIdentifier.identifier", "gender", "birthdate", "personAddress"};

    public static final String[] MPI_PATIENT_PROPERTIES = new String[]{"patientId", "givenName", "familyName",
            "patientIdentifier.identifier", "gender", "birthdate", "personAddress", "mpiPatient"};

    public List<SimpleObject> getSimilarPatients(@RequestParam("appId") AppDescriptor app,
	                                             @SpringBean("registrationCoreService") RegistrationCoreService service,
	                                             @ModelAttribute("patient") @BindParams Patient patient,
	                                             @ModelAttribute("personName") @BindParams PersonName name,
	                                             @ModelAttribute("personAddress") @BindParams PersonAddress address,
	                                             HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);
		List<PatientAndMatchQuality> matches = service.findFastSimilarPatients(patient, null, CUTOFF, MAX_RESULTS);
        return simplify(ui, matches, PATIENT_PROPERTIES);
	}

    public List<SimpleObject> getExactPatients(@RequestParam("appId") AppDescriptor app,
                                               @SpringBean("registrationCoreService") RegistrationCoreService service,
                                               @ModelAttribute("patient") @BindParams Patient patient,
                                               @ModelAttribute("personName") @BindParams PersonName name,
                                               @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                               HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);
        List<SimpleObject> result = new ArrayList<SimpleObject>();

        List<SimpleObject> localExactMatches = getLocalExactMatches(service, patient, ui, MAX_RESULTS);
        result.addAll(localExactMatches);

        if (result.size() < MAX_RESULTS) {
            List<SimpleObject> mpiExactMatches = getMpiExactMatches(service, patient, ui, MAX_RESULTS - result.size());
            result.addAll(mpiExactMatches);
        }

        return result;
    }

    private void addToPatient(Patient patient, AppDescriptor app, PersonName name, PersonAddress address, HttpServletRequest request) throws IOException {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        patient.addName(name);
        patient.addAddress(address);

        if (formStructure != null) {
            RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
        }
    }

    private List<SimpleObject> getLocalExactMatches(RegistrationCoreService service,
                                                    Patient patient, UiUtils ui, int maxResults) {
        List<PatientAndMatchQuality> localMatches =
                service.findPreciseSimilarPatients(patient, null, CUTOFF, maxResults);

        return simplify(ui, localMatches, PATIENT_PROPERTIES);
    }

    private List<SimpleObject> getMpiExactMatches(RegistrationCoreService service,
                                                  Patient patient, UiUtils ui, int maxResults) {
        List<PatientAndMatchQuality> mpiMatches =
                service.findPreciseSimilarPatientsOnMpi(patient, null, CUTOFF, maxResults);

        return simplify(ui, mpiMatches, MPI_PATIENT_PROPERTIES);
    }

    private List<SimpleObject> simplify(UiUtils ui, List<PatientAndMatchQuality> matches, String[] properties) {
        List<Patient> similarPatients = new ArrayList<Patient>();
        for (PatientAndMatchQuality match : matches) {
            similarPatients.add(match.getPatient());
        }
        return SimpleObject.fromCollection(similarPatients, ui, properties);
    }
}
