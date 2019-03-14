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
import java.util.List;
import org.junit.Test;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class RegistrationSummaryExtensionsGeneratorTest {

	private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
	
    @Test
    public void generate_shouldGenerateRegSummaryFromRegApp() throws Exception{
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream("registration_app.json");    	
    	List<AppDescriptor> appDescriptors = new ObjectMapper().readValue(inputStream, new TypeReference<List<AppDescriptor>>() {});
    	
    	List<Extension> extensions = RegistrationSummaryExtensionsGenerator.generate(appDescriptors.get(0));
    	
    	assertNotNull(extensions);
    	assertEquals(4, extensions.size());
    	for (Extension extn : extensions) {
	    	assertNotNull(extn.getId());
	    	assertNotNull(extn.getAppId());
	    	assertNotNull(extn.getExtensionPointId());
	    	assertNotNull(extn.getExtensionParams());
    	}
    	
    }
}
