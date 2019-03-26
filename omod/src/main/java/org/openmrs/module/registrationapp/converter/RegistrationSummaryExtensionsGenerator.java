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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;

public class RegistrationSummaryExtensionsGenerator {
	
	/**
	 * Generates a best match registration summary widgets list of extensions
	 * given a registration app configuration.
	 * 
	 * @param app The app descriptor for the registration app.
	 * @param distribute flag to distribute summary widgets into columns or not
	 * @return The list of registration summary widgets extensions.
	 * 
	 * @throws IllegalArgumentException
	 * 
	 * @should throw when the provided app is not a registration app
	 */
	public static List<Extension> generate(AppDescriptor app, boolean distribute) throws IllegalArgumentException {
		
		if (!isRegAppConfig(app)) {
			throw new IllegalArgumentException("Not a Registration application configuration.");
		}
		
		List<Extension> extensions = new ArrayList<Extension>();
		JsonNode sections = app.getConfig().get("sections");
		
		if (sections != null && sections.size() > 0 ) {
			boolean firstColumn = false;
			for (JsonNode section : sections) {
				String sectionId = section.get("id").getTextValue();
				if ("contactInfo".equalsIgnoreCase(sectionId) || "demographics".equalsIgnoreCase(sectionId)){
					// 'contact info' and 'demographics' are hardcoded in the reg. summary dashboard view
					continue;
				}
				
				String appId = app.getId();
				String regSummaryId = generateRegSummaryId(appId, section.get("id").getTextValue());
				String extensionPointId = (firstColumn = !firstColumn) || !distribute ? "registrationSummary.contentFragments" 
				                                                                      : "registrationSummary.secondColumnContentFragments";
				
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
	 * @return true if the app is a registration app, false otherwise
	 */
	public static boolean isRegAppConfig(AppDescriptor app) {
		return (app.getInstanceOf() != null && "registrationapp.registerPatient".equals(app.getInstanceOf()));
	}
	
	/**
	 * Generates a comprehensive registration summary ID from an app ID and section ID.
	 */
	public static String generateRegSummaryId(String regAppId, String sectionId) {
		String regSummaryId = regAppId.substring(0, regAppId.indexOf("registerPatient")) 
								+ "summary." 
								+ sectionId;
		return regSummaryId;
	}
}