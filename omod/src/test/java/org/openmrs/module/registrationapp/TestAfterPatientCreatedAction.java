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
package org.openmrs.module.registrationapp;

import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationapp.action.AfterPatientCreatedAction;

import java.util.Map;

public class TestAfterPatientCreatedAction implements AfterPatientCreatedAction {

	@Override
	public void afterPatientCreated(Patient created, Map<String, String[]> submittedParameters) {
		if (submittedParameters != null) {
			for (String key : submittedParameters.keySet()) {
				String[] vals = submittedParameters.get(key);
				if (vals != null && vals.length > 0) {
					if (key.equals("throwError")) {
						throw new IllegalArgumentException(vals[0]);
					}
					else if (key.equals("firstName") || key.equals("lastName")) {
						PersonName pn = created.getPersonName();
						if (key.equals("firstName")) {
							pn.setGivenName(vals[0]);
						} else {
							pn.setFamilyName(vals[0]);
						}
						Context.getPatientService().savePatient(created);
					}
				}
			}
		}
	}
}
