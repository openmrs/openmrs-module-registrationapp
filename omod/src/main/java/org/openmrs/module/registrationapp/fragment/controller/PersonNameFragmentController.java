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
import java.util.List;

import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

public class PersonNameFragmentController {

	public SimpleObject getSimilarNames(
			@SpringBean("registrationCoreService") RegistrationCoreService service,
			@RequestParam(value = "searchPhrase", required = true) String searchPhrase,
			@RequestParam(value = "formFieldName", required = true) String formFieldName) {

		List<String> names = new ArrayList<String>();

		if ("givenName".equals(formFieldName)) {
			names.addAll(service.findSimilarGivenNames(searchPhrase));
		} 
		else if ("familyName".equals(formFieldName)) {
			names.addAll(service.findSimilarFamilyNames(searchPhrase));
		}

		SimpleObject result = new SimpleObject();
		result.put("names", names);

		return result;
	}
}
