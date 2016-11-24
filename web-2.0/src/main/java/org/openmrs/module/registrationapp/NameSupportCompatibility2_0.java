/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationapp;

import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.layout.name.NameSupport;
import org.springframework.stereotype.Component;

@Component("registrationapp.NameSupportCompatibility")
@OpenmrsProfile(openmrsPlatformVersion = "2.*")
public class NameSupportCompatibility2_0 implements NameSupportCompatibility {

	@Override
	public Object getDefaultLayoutTemplate() {
		return NameSupport.getInstance().getDefaultLayoutTemplate();
	}
}
