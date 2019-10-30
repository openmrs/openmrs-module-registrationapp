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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationapp.model.Question;
import org.openmrs.module.registrationapp.model.Section;
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
		assertEquals(2.0, fc.get("min"));
		assertEquals(4.0, fc.get("max"));
		assertEquals(60, fc.get("size"));
		assertEquals(true, fc.get("resizeable"));
	}
	
	/**
	 * @see RegisterPatientFormBuilder#buildFormStructure(AppDescriptor,Boolean)
	 * @verifies combbining of config sections
	 */
	@Test
	public void buildFormStructure_shouldCombineConfigSectionsIntoOne() throws Exception {
		// setup
		ObjectMapper mapper = new ObjectMapper();
		List<AppDescriptor> appDescriptors = mapper.readValue(getClass().getClassLoader().getResourceAsStream("registration_app.json"), new TypeReference<List<AppDescriptor>>() {});
		AppDescriptor appDescriptor = appDescriptors.get(0);
		List<String> questionIds = new ArrayList<String>();
		
		Iterator<JsonNode> sectionIt = appDescriptor.getConfig().get("sections").getElements();
		while(sectionIt.hasNext()) {
			Iterator<JsonNode> questionIt = sectionIt.next().get("questions").getElements();
		    while(questionIt.hasNext()) {
		    	questionIds.add(questionIt.next().get("id").getTextValue());
		    }
		}
		
		// replay
		NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(appDescriptor, true);
		
		// verify
		Map<String, Section> sections = formStructure.getSections(); 
		assertEquals(1, sections.size());
		
		Section combinedSection = sections.get(RegisterPatientFormBuilder.DEMOGRAPHICS_SECTION_ID);
		assertNotNull(combinedSection);		
		for (Question question : combinedSection.getQuestions()) {
			assertTrue(questionIds.contains(question.getId()));
		}		
	}
}
