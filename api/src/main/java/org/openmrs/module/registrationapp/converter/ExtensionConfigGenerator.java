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

import org.codehaus.jackson.JsonNode;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.domain.AppDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ExtensionConfigGenerator {
	
	public static List<Extension> generate(AppDescriptor app) {
	
		List<Extension> extensions = new ArrayList<Extension>();
		JsonNode sections = app.getConfig().get("sections");
		
		if (sections.size() > 0) {
			for(JsonNode section : sections) {
				
				String id = app.getId() + section.get("id").getTextValue();
				String appId = app.getId();
				String extensionPointId = "registrationSummary.contentFragments";
				
				Map<String, Object> extensionParams = new HashMap<String, Object>();
				Map<String, String> fragmentConfig = new HashMap<String, String>();
				fragmentConfig.put("sectionId", section.get("id").getTextValue());
				
				extensionParams.put("provider", "registrationapp");
				extensionParams.put("fragment", "summary/section");
				extensionParams.put("fragmentConfig", fragmentConfig);
			
				Extension extension = new Extension(id
													,appId
													,extensionPointId
													,null, null, null, 1, null
													,extensionParams);
				extensions.add(extension);	
			}
		}
		
		return extensions;
	}
}
