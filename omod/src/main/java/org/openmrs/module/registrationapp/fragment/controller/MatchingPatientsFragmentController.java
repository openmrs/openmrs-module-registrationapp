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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
public class MatchingPatientsFragmentController {
	
	public List<SimpleObject> getSimilarPatients(@SpringBean("registrationCoreService") RegistrationCoreService service,
	                                             @RequestParam(value = "givenName", required = false) String givenName,
	                                             @RequestParam(value = "familyName", required = false) String familyName,
	                                             @RequestParam(value = "gender", required = false) String gender,
	                                             @RequestParam(value = "birthDay", required = false) Integer birthDay,
	                                             @RequestParam(value = "birthMonth", required = false) Integer birthMonth,
	                                             @RequestParam(value = "birthYear", required = false) Integer birthYear,
	                                             UiUtils ui) {
		Patient patient = new Patient();
		patient.addName(new PersonName(givenName, null, familyName));
		if (!StringUtils.isBlank(gender)) {
			patient.setGender(gender);
		}
		Calendar calendar = Calendar.getInstance();
		if (birthYear != null) {
			calendar.set(Calendar.YEAR, birthYear);
			patient.setBirthdate(calendar.getTime());
		}
		if (birthMonth != null) {
			calendar.set(Calendar.MONTH, birthMonth - 1);
			patient.setBirthdate(calendar.getTime());
		}
		if (birthDay != null) {
			calendar.set(Calendar.DATE, birthDay);
			patient.setBirthdate(calendar.getTime());
		}
		List<PatientAndMatchQuality> matches = service.findFastSimilarPatients(patient, null, 2.0, 10);
		
		List<Patient> similarPatients = new ArrayList<Patient>();
		for (PatientAndMatchQuality match : matches) {
			similarPatients.add(match.getPatient());
		}
		
		String[] propertiesToInclude = new String[] { "patientId", "givenName", "familyName", "patientIdentifier.identifier", "gender", "birthdate", "personAddress" };
		
		return SimpleObject.fromCollection(similarPatients, ui, propertiesToInclude);
	}
}
