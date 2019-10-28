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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.Relationship;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.idgen.EmptyIdentifierPoolException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
	 * @since 1.15.0
	 * Gets the person attribute display value for the specified person attribute type uuid
	 * @return the person attribute display value
	 */
	public String getPersonAttributeDisplayValue(Person person, String attributeTypeUuid) {
		if (person != null) {
			PersonAttribute attr = person.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
					attributeTypeUuid));
			if (attr != null) {
				return attr.toString();
			}
		}

		return null;
	}
	
	/**
	 * @since 1.15.0
	 * 
	 * Gets the patient's relationships
	 * 
	 * @return the patient's relationships
	 */
	public String getPatientRelationships(Person person) {
		StringBuilder rels = new StringBuilder("");
		if (person != null) {
			List<Relationship> relationships = Context.getPersonService().getRelationshipsByPerson(person);
			for (Relationship relationship : relationships) {
				if(relationship.getPersonA().getUuid() != person.getUuid()){
					rels.append(relationship.getPersonA().getPersonName()).append(" - ").append(relationship.getRelationshipType().getaIsToB());
                } else {
                	rels.append(relationship.getPersonB().getPersonName()).append(" - ").append(relationship.getRelationshipType().getbIsToA());
                }
				rels.append(", ");
			}
		}
		return rels.toString();
	}

	/**
	 * Gets the patient identifier value for the specified person for the identifierTypeUuid
	 * that matches the specified uuid
	 *
	 * @return the identifier value
	 */
	public String getIdentifier(Patient patient, String identifierTypeUuid) {
		if (patient != null) {
				PatientIdentifier patientIdentifier = patient.getPatientIdentifier(Context.getPatientService().getPatientIdentifierTypeByUuid(identifierTypeUuid));
				if (patientIdentifier != null) {
					return patientIdentifier.getIdentifier();
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

	public static void checkForIdentifierExceptions(Exception ex, Errors errors) {
		// add any patient identifier validation exceptions
		if (ex instanceof PatientIdentifierException) {
			if (ex instanceof InvalidCheckDigitException) {
				errors.reject("registrationapp.error.identifier.invalidCheckDigit");
			}
			else if (ex instanceof DuplicateIdentifierException) {
				errors.reject("registrationapp.error.identifier.duplicate", Collections.singleton(((DuplicateIdentifierException) ex).getPatientIdentifier()).toArray(), null);
			}
			else if (ex instanceof IdentifierNotUniqueException) {
				errors.reject("registrationapp.error.identifier.duplicate", Collections.singleton(((IdentifierNotUniqueException) ex).getPatientIdentifier()).toArray(), null);
			}
			else {
				errors.reject("registrationapp.error.identifier.general");
			}
		}
		else if (ex instanceof EmptyIdentifierPoolException) {
			errors.reject("registrationapp.error.identifier.emptyIdentifierPool");
		}
	}
	
	/**
	 * @since 1.15.0
	 * 
     * Reads the 'genderOptions' app's config value and returns the specified gender options as string array
     * otherwise returns the default gender options string array
     * 
     * @param app that may provide gender options if it is configured
     * @return string array of gender options
     */
    public static String[] getGenderOptions(AppDescriptor app) {
    	if (app.getConfig().get("genderOptions") != null) {
    		return app.getConfig().get("genderOptions").getTextValue().replace(" ", "").split(",");
    	}
    	return new String[] {"M", "F"};
    }


}
