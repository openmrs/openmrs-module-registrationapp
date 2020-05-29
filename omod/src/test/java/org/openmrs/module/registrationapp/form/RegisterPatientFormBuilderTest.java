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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Requireable;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appframework.service.AppFrameworkServiceImpl;
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

	@Test
	public void buildFormStructure_shouldExcludeSectionsBasedOnRequireProperty() throws Exception {
		// setup
		ObjectMapper mapper = new ObjectMapper();
		List<AppDescriptor> appDescriptors = mapper.readValue(getClass().getClassLoader().getResourceAsStream("registration_app_with_require.json"), new TypeReference<List<AppDescriptor>>() {});
		AppDescriptor appDescriptor = appDescriptors.get(0);

		AppFrameworkService appFrameworkService = mock(AppFrameworkService.class);
		// our silly rule: if an item has a required field, exclude it
		when(appFrameworkService.checkRequireExpression(any(Requireable.class), any(AppContextModel.class))).then(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocationOnMock) {
				Requireable requireable = (Requireable) invocationOnMock.getArguments()[0];
				return StringUtils.isBlank(requireable.getRequire());
			}
		});

		// replay
		NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(appDescriptor, false, appFrameworkService, new AppContextModel());

		// verify
		Map<String, Section> sections = formStructure.getSections();
		assertThat(sections.containsKey("nextOfKin"), is(false));

		Section demographics = sections.get("contactInfo");
		for (Question question : demographics.getQuestions()) {
			assertThat(question.getId(), not(equalTo("phoneNumber")));
		}
	}
}
