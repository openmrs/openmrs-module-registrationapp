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

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.springframework.validation.BindingResult;

public class RegistrationAppUiUtils {
	
	//In general, the latitude and longitude values can have any number of decimal places 
	//but optional, can start with an optional + or -, can't start with a decimal point,
	//If it has a decimal point, require at least one decimal place
	
	//Whole numbers valid ranges are 0-89 or 90
	public static final String DEFAULT_LATITUDE_REGEX = "[+-]?((([0-8]?[0-9])(\\.\\d+)?)|90(\\.0+)?)";
	
	//Whole numbers valid ranges are 0-179 or 180
	public static final String DEFAULT_LONGITUDE_REGEX = "[+-]?((((1?[0-7]?|[0-9]?)[0-9])(\\.\\d+)?)|180(\\.0+)?)";
	
	/**
	 * Gets the person attribute value for the specified person for the getPersonAttributeTypeByUuid
	 * that matches the specified uuid
	 * 
	 * @return the attribute value
	 */
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
	
	/**
	 * Validates the specified latitude value against the default regex
	 * #DEFAULT_LATITUDE_REGEX_FORMAT
	 * 
	 * @param latitude
	 * @return true if the latitude value if true otherwise false
	 * @should pass for a valid latitude value
	 * @should fail for an invalid latitude value
	 */
	public static boolean isValidLatitude(String latitude) {
		return isValid(latitude, DEFAULT_LATITUDE_REGEX);
	}
	
	/**
	 * Validates the specified longitude value against the default regex
	 * #DEFAULT_LONGITUDE_REGEX_FORMAT
	 * 
	 * @param longitude
	 * @return true if the latitude value if true otherwise false
	 * @should pass for a valid longitude value
	 * @should fail for an invalid longitude value
	 */
	public static boolean isValidLongitude(String longitude) {
		return isValid(longitude, DEFAULT_LONGITUDE_REGEX);
	}
	
	private static boolean isValid(String value, String regex) {
		return Pattern.compile(regex).matcher(value).matches();
	}
	
	public static void validateLatitudeAndLongitudeIfNecessary(PersonAddress address, BindingResult errors) {
		if (address != null) {
			
	        Map<String, String> regex = Context.getRegisteredComponent(AddressSupportCompatibility.ID, AddressSupportCompatibility.class).getElementRegex();

			if (StringUtils.isNotBlank(address.getLatitude())) {
				if (regex != null && StringUtils.isBlank(regex.get("latitude"))) {
					if (!RegistrationAppUiUtils.isValidLatitude(address.getLatitude())) {
						errors.reject("registrationapp.latitude.invalid");
					}
				}
			}
			
			if (StringUtils.isNotBlank(address.getLongitude())) {
				if (regex != null && StringUtils.isBlank(regex.get("longitude"))) {
					if (!RegistrationAppUiUtils.isValidLongitude(address.getLongitude())) {
						errors.reject("registrationapp.longitude.invalid");
					}
				}
			}
		}
	}
	
	/**
	 * Get the defined custom registration appId from the global property value registrationcore.customRegistrationAppId defined by
	 *
	 * @see RegistrationAppConstants#GP_CUSTOM_REGISTRATION_APPID
	 *
	 * @return the custom appId or null if none is defined
	 */
	public static String getCustomRegistrationAppId() {
		// check if there is a custom registration app registered then use that
		String customRegistratonAppId = Context.getAdministrationService().getGlobalProperty(RegistrationAppConstants.GP_CUSTOM_REGISTRATION_APPID);
		if (StringUtils.isNotBlank(customRegistratonAppId)) {
			return customRegistratonAppId;
		}
		
		return null;
	}
	
	/**
	 * Get the registration appId
	 *
	 * @return the Reference Application Registration appId or the custom one if defined by the global property {@link RegistrationAppConstants#GP_CUSTOM_REGISTRATION_APPID}
	 */
	public static String getDefaultRegistrationAppId() {
		String customAppId = getCustomRegistrationAppId();
		if (StringUtils.isBlank(customAppId)) {
			return "referenceapplication.registrationapp.registerPatient";
		} else {
			return customAppId;
		}
	}
}
