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
package org.openmrs.module.registrationapp.form;

import static org.junit.Assert.assertEquals;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

public class RegisterPatientFormBuilderTest {
	
	/**
	 * @see RegisterPatientFormBuilder#buildFormStructure(AppDescriptor)
	 * @verifies flatten the widget config
	 */
	@Test
	public void buildFormStructure_shouldFlattenTheWidgetConfig() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		AppDescriptor appDescriptor = mapper.readValue(
		    getClass().getClassLoader().getResourceAsStream("test_app_config.json"), AppDescriptor.class);
		NavigableFormStructure fs = RegisterPatientFormBuilder.buildFormStructure(appDescriptor);
		assertEquals(1, fs.getFields().size());
		FragmentConfiguration fc = fs.getFields().get(0).getFragmentRequest().getConfiguration();
		assertEquals("someRegex", fc.getAttribute("regex"));
		assertEquals("2.0", ((JsonNode) fc.get("min")).getValueAsText());
		assertEquals("4.0", ((JsonNode) fc.get("max")).getValueAsText());
		assertEquals("60", ((JsonNode) fc.get("size")).getValueAsText());
	}
}
