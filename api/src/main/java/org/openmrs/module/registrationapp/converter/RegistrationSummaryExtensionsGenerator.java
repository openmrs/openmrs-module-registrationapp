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

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This class is used to generate Registration summary configuration
 * based on the way the Registration Sections are configured in the 
 * configuration passed in through AppDescrtiptor 
 */
public class RegistrationSummaryExtensionsGenerator {
	
	/**
	 * Generates regestration summary configuration from registration 
	 * application config 
	 * 
	 * @param app contains the registration application configuration
	 * @return list of registration summary configuration
	 * @throws IOException
	 * @should generate summary config from AppDescriptor(app config)
	 * @should throw exception if config is not for registrationapp
	 * @should return empty Extension list if app config has no sections
	 */
	public static List<Extension> generate(AppDescriptor app) throws IllegalArgumentException {
		
		if (!isRegAppConfig(app)) {
			throw new IllegalArgumentException("Not a Registration application configuration.");
		}
		
		List<Extension> extensions = new ArrayList<Extension>();
		JsonNode sections = app.getConfig().get("sections");
		
		if (sections != null && sections.size() > 0 ) {
			for (JsonNode section : sections) {
				String sectionId = section.get("id").getTextValue();
				if (sectionId.equalsIgnoreCase("contactInfo") || sectionId.equalsIgnoreCase("demographics")){
					// 'contact info' and 'demographics' are hardcoded in the reg. summary dashboard view
					continue;
				}
				
				String appId = app.getId();
				String regSummaryId = generateRegSummaryId(appId, section.get("id").getTextValue());
				String extensionPointId = "registrationSummary.contentFragments";
				
				Map<String, Object> extensionParams = new HashMap<String, Object>();
				Map<String, String> fragmentConfig = new HashMap<String, String>();
				fragmentConfig.put("sectionId", section.get("id").getTextValue());
				
				extensionParams.put("provider", "registrationapp");
				extensionParams.put("fragment", "summary/section");
				extensionParams.put("fragmentConfig", fragmentConfig);
			
				Extension extension = new Extension(regSummaryId
													,appId
													,extensionPointId
													,null, null, null, 1, null
													,extensionParams);
				extensions.add(extension);	
			}
		}
		return extensions;
	}

	/**
	 * Checks whether configuration passed is for the registration app
	 * 
	 * @param app contains the registration application configuration
	 * @return true/false
	 */
	public static Boolean isRegAppConfig(AppDescriptor app) {
		return (app.getId() != null && app.getId().contains("registrationapp.registerPatient"));
	}
	
	/**
	 * Generates registration summary Id from the appId and sectionId
	 * 
	 * @param regAppId contains the registration application Id
	 * @param sectionId contains the registratin application section Id
	 * @return registration summary configuration Id
	 */
	public static String generateRegSummaryId (String regAppId, String sectionId) {
		String regSummaryId = regAppId.substring(0, regAppId.indexOf("registerPatient")) 
								+ "summary." 
								+ sectionId;
		return regSummaryId;
	}
}
