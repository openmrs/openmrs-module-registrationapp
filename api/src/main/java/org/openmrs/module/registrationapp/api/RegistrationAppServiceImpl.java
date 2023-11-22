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
package org.openmrs.module.registrationapp.api;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.registrationapp.action.AfterPatientCreatedAction;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * This service provides transactional services for the registration app module
 */
@Transactional
public class RegistrationAppServiceImpl extends BaseOpenmrsService implements RegistrationAppService {

	private RegistrationCoreService registrationCoreService;
	private EncounterService encounterService;
	private ObsService obsService;

	@Override
	public Patient registerPatient(RegistrationAppData registrationAppData) {
		Patient patient = registrationAppData.getRegistrationData().getPatient();

		registrationCoreService.registerPatient(registrationAppData.getRegistrationData());

		Encounter registrationEncounter = registrationAppData.getRegistrationEncounter();
		for (Obs o : registrationAppData.getRegistrationObs()) {
			if (registrationEncounter != null) {
				registrationEncounter.addObs(o);
			}
			else {
				obsService.saveObs(o, null);
			}
		}
		encounterService.saveEncounter(registrationEncounter);

		for (AfterPatientCreatedAction action : registrationAppData.getAfterRegistrationActions().keySet()) {
			Map<String, String[]> parameters = registrationAppData.getAfterRegistrationActions().get(action);
			action.afterPatientCreated(patient, parameters);
		}

		return patient;
	}

	public void setRegistrationCoreService(RegistrationCoreService registrationCoreService) {
		this.registrationCoreService = registrationCoreService;
	}

	public void setEncounterService(EncounterService encounterService) {
		this.encounterService = encounterService;
	}

	public void setObsService(ObsService obsService) {
		this.obsService = obsService;
	}
}
