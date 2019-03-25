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
package org.openmrs.module.registrationapp.converter;

import java.io.InputStream;
import java.lang.IllegalArgumentException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.junit.Test;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.domain.AppDescriptor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

public class RegistrationSummaryExtensionsGeneratorTest {

	
    @Test
    public void generate_shouldGenerateRegSummaryFromRegApp() throws Exception {
    	// setup
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream("registration_app.json");    	
    	List<AppDescriptor> appDescriptors = new ObjectMapper().readValue(inputStream, new TypeReference<List<AppDescriptor>>() {});
    	
    	AppDescriptor appDescriptor = appDescriptors.get(0);
    	JsonNode sections = appDescriptor.getConfig().get("sections");
    	
    	// replay
    	List<Extension> extensions = RegistrationSummaryExtensionsGenerator.generate(appDescriptor, false);
    	
    	// verify
    	assertNotNull(extensions);
    	assertEquals(3, extensions.size());
    	for (Extension extn : extensions) {
    		//For each Extension, loop over all appDescriptor's sections
	    	for (JsonNode section : sections) {
	    		String sectionId = section.get("id").getTextValue();
	    		
	    		if (extn.getId().contains(sectionId)) {
					assertNotNull(extn.getId());
					assertEquals("referenceapplication.registrationapp.summary." + sectionId, extn.getId());
					
					assertNotNull(extn.getAppId());
					assertEquals(appDescriptor.getId(), extn.getAppId());
					
					assertNotNull(extn.getExtensionPointId());
					assertEquals("registrationSummary.contentFragments", extn.getExtensionPointId());
					
					assertNotNull(extn.getExtensionParams());
					assertEquals("summary/section", extn.getExtensionParams().get("fragment"));
					assertEquals("registrationapp", extn.getExtensionParams().get("provider"));
					
					Map<String, String> fragmentConfig = (HashMap<String, String>) extn.getExtensionParams().get("fragmentConfig");
					assertEquals(sectionId, fragmentConfig.get("sectionId"));
					break;
				}
			}
    	}
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void generate_shouldThrowWhenNotRegAppConfig() throws Exception {
    	// setup
    	AppDescriptor appDescriptor = new AppDescriptor("fooId", "fooDesc", null, null, null, null, 0);
    	appDescriptor.setInstanceOf("not.a.reg.app");
    	
    	// replay
    	RegistrationSummaryExtensionsGenerator.generate(appDescriptor, true);
    }
    
    @Test
    public void generate_shouldReturnEmptyWhenEmptySectionsInConfig() throws Exception {
    	// setup
    	AppDescriptor appDesc = new AppDescriptor("my.registrationapp.registerPatient", "Create a new Patient Record", "Register Patient", null, null, null, 0, null, null);
    	appDesc.setInstanceOf("registrationapp.registerPatient");
    	
    	ObjectNode config = new ObjectMapper().createObjectNode();
    	config.putArray("sections"); 
    	appDesc.setConfig(config);
    	
    	// replay
    	List<Extension> regSummaryExtensions = RegistrationSummaryExtensionsGenerator.generate(appDesc, true);

    	// verify
    	assertThat(regSummaryExtensions, is(empty()));
    }
}
