/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationapp.api;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.registrationapp.action.AfterPatientCreatedAction;
import org.openmrs.module.registrationcore.RegistrationData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the data that may be collected and saved within a patient registration transaction
 */
public class RegistrationAppData implements Serializable {

	private RegistrationData registrationData;
	private Encounter registrationEncounter;
	private List<Obs> registrationObs;
	private Map<AfterPatientCreatedAction, Map<String, String[]>> afterRegistrationActions;

	public RegistrationAppData() {
	}

	public RegistrationData getRegistrationData() {
		return registrationData;
	}

	public void setRegistrationData(RegistrationData registrationData) {
		this.registrationData = registrationData;
	}

	public Encounter getRegistrationEncounter() {
		return registrationEncounter;
	}

	public void setRegistrationEncounter(Encounter registrationEncounter) {
		this.registrationEncounter = registrationEncounter;
	}

	public List<Obs> getRegistrationObs() {
		if (registrationObs == null) {
			registrationObs = new ArrayList<Obs>();
		}
		return registrationObs;
	}

	public void setRegistrationObs(List<Obs> registrationObs) {
		this.registrationObs = registrationObs;
	}

	public Map<AfterPatientCreatedAction, Map<String, String[]>> getAfterRegistrationActions() {
		if (afterRegistrationActions == null) {
			afterRegistrationActions = new LinkedHashMap<AfterPatientCreatedAction, Map<String, String[]>>();
		}
		return afterRegistrationActions;
	}

	public void setAfterRegistrationActions(Map<AfterPatientCreatedAction, Map<String, String[]>> afterRegistrationActions) {
		this.afterRegistrationActions = afterRegistrationActions;
	}
}
