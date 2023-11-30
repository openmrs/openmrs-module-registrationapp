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
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.module.registrationapp.action.AfterPatientCreatedAction;
import org.openmrs.module.registrationcore.RegistrationData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the data that may be collected and saved within a patient registration transaction
 */
public class RegistrationAppData implements Serializable {

	private Date registrationDate;
	private Location registrationLocation;
	private RegistrationData registrationData;
	private Encounter registrationEncounter;
	private List<Obs> registrationObs;
	private List<AfterPatientCreatedAction> afterPatientCreatedActions;
	private Map<String, String[]> parameters;

	public RegistrationAppData() {
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Location getRegistrationLocation() {
		return registrationLocation;
	}

	public void setRegistrationLocation(Location registrationLocation) {
		this.registrationLocation = registrationLocation;
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

	public List<AfterPatientCreatedAction> getAfterPatientCreatedActions() {
		if (afterPatientCreatedActions == null) {
			afterPatientCreatedActions = new ArrayList<AfterPatientCreatedAction>();
		}
		return afterPatientCreatedActions;
	}

	public void setAfterPatientCreatedActions(List<AfterPatientCreatedAction> afterPatientCreatedActions) {
		this.afterPatientCreatedActions = afterPatientCreatedActions;
	}

	public Map<String, String[]> getParameters() {
		if (parameters == null) {
			parameters = new HashMap<String, String[]>();
		}
		return parameters;
	}

	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}
}
