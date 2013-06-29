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

import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;

public class RegistrationAppUiUtils {
	
	public String getAttribute(Person person, String attributeTypeUuid) {
		if (person != null) {
			PersonAttribute attr = person.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
			    attributeTypeUuid));
			if (attr != null) {
				return attr.getValue();
			}
		}
		
		return null;
	}
}
