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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openmrs.module.registrationapp.RegistrationAppUiUtils.isValidLatitude;
import static org.openmrs.module.registrationapp.RegistrationAppUiUtils.isValidLongitude;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
public class RegistrationAppUiUtilsTest {
	
	private List<Relationship> relationships;
	
	private Person patient;
	
	private Person person1;
	
	private Person person2;
	
	private RelationshipType relType1;
	
	private RelationshipType relType2;
	
	@Mock
	private PersonService personService;
	
	@Before
	public void setup() {
		patient = new Person();
		person1 = new Person();
		person1.addName(new PersonName("Person1GivenName", null, "Person1FamilyName"));
		person2 = new Person();
		person2.addName(new PersonName("Person2GivenName", null, "Person2FamilyName"));
		
		relType1 = new RelationshipType();
		relType1.setaIsToB("Parent");
		relType2 = new RelationshipType();
		relType2.setbIsToA("Uncle");
		
		relationships = new ArrayList<Relationship> ();
		relationships.add(new Relationship(person1, patient, relType1));
		relationships.add(new Relationship(patient, person2, relType2));
		
		mockStatic(Context.class);
		when(Context.getPersonService()).thenReturn(personService);
		when(personService.getRelationshipsByPerson(patient)).thenReturn(relationships);
	}
	
	/**
	 * @verifies pass for a valid latitude value
	 * @see RegistrationAppUiUtils#isValidLatitude(String)
	 */
	@Test
	public void isValidLatitude_shouldPassForAValidLatitudeValue() throws Exception {
		assertEquals(true, isValidLatitude("0"));
		assertEquals(true, isValidLatitude("0.0"));
		assertEquals(true, isValidLatitude("4"));
		assertEquals(true, isValidLatitude("+4"));
		assertEquals(true, isValidLatitude("4.0"));
		assertEquals(true, isValidLatitude("34"));
		assertEquals(true, isValidLatitude("-34"));
		assertEquals(true, isValidLatitude("+34.123456"));
		assertEquals(true, isValidLatitude("-34.123456"));
		assertEquals(true, isValidLatitude("89.0"));
		assertEquals(true, isValidLatitude("89.9999999"));
		assertEquals(true, isValidLatitude("+89.0"));
		assertEquals(true, isValidLatitude("+89.999999"));
		assertEquals(true, isValidLatitude("+90"));
		assertEquals(true, isValidLatitude("-90"));
		assertEquals(true, isValidLatitude("+90.0"));
		assertEquals(true, isValidLatitude("-90.0"));
		assertEquals(true, isValidLatitude("+90.00"));
	}
	
	/**
	 * @verifies fail for an invalid latitude value
	 * @see RegistrationAppUiUtils#isValidLatitude(String)
	 */
	@Test
	public void isValidLatitude_shouldFailForAnInvalidLatitudeValue() throws Exception {
		assertEquals(false, isValidLatitude("*34.123456"));
		assertEquals(false, isValidLatitude("+tt.ttttt"));
		assertEquals(false, isValidLatitude("++34.123456"));
		assertEquals(false, isValidLatitude("+34..12345"));
		assertEquals(false, isValidLatitude("++34.123"));
		assertEquals(false, isValidLatitude("+34.1 23"));
		assertEquals(false, isValidLatitude("011.12345"));
		assertEquals(false, isValidLatitude("t34.12345"));
		assertEquals(false, isValidLatitude("34.s"));
		assertEquals(false, isValidLatitude("+91"));
		assertEquals(false, isValidLatitude("91"));
		assertEquals(false, isValidLatitude("91.0"));
		assertEquals(false, isValidLatitude("101"));
		assertEquals(false, isValidLatitude("+91.123456"));
		assertEquals(false, isValidLatitude("-91.12345"));
		assertEquals(false, isValidLatitude("+3 .908"));
		assertEquals(false, isValidLatitude("+ 4.908"));
		assertEquals(false, isValidLatitude("+34. 908"));
		assertEquals(false, isValidLatitude("+.908"));
		assertEquals(false, isValidLatitude(".908"));
		assertEquals(false, isValidLatitude("-90.1"));
		assertEquals(false, isValidLatitude("+90.01"));
		assertEquals(false, isValidLatitude("+901"));
		assertEquals(false, isValidLatitude("901"));
		assertEquals(false, isValidLatitude("90."));
		assertEquals(false, isValidLatitude("+90."));
		assertEquals(false, isValidLatitude("34."));
		assertEquals(false, isValidLatitude("+34."));
	}
	
	/**
	 * @verifies pass for a valid longitude value
	 * @see RegistrationAppUiUtils#isValidLongitude(String)
	 */
	@Test
	public void isValidLongitude_shouldPassForAValidLongitudeValue() throws Exception {
		assertEquals(true, isValidLongitude("0"));
		assertEquals(true, isValidLongitude("0.0"));
		assertEquals(true, isValidLongitude("4"));
		assertEquals(true, isValidLongitude("+4"));
		assertEquals(true, isValidLongitude("4.0"));
		assertEquals(true, isValidLongitude("34"));
		assertEquals(true, isValidLongitude("-34"));
		assertEquals(true, isValidLongitude("+34.123456"));
		assertEquals(true, isValidLongitude("-34.123456"));
		assertEquals(true, isValidLongitude("179.0"));
		assertEquals(true, isValidLongitude("179.9999999"));
		assertEquals(true, isValidLongitude("+179.0"));
		assertEquals(true, isValidLongitude("+179.999999"));
		assertEquals(true, isValidLongitude("+180"));
		assertEquals(true, isValidLongitude("-180"));
		assertEquals(true, isValidLongitude("+180.0"));
		assertEquals(true, isValidLongitude("-180.0"));
		assertEquals(true, isValidLongitude("+180.00"));
	}
	
	/**
	 * @verifies fail for an invalid longitude value
	 * @see RegistrationAppUiUtils#isValidLongitude(String)
	 */
	@Test
	public void isValidLongitude_shouldFailForAnInvalidLongitudeValue() throws Exception {
		assertEquals(false, isValidLongitude("*34.123456"));
		assertEquals(false, isValidLongitude("+tt.ttttt"));
		assertEquals(false, isValidLongitude("++34.123456"));
		assertEquals(false, isValidLongitude("+34..12345"));
		assertEquals(false, isValidLongitude("011.12345"));
		assertEquals(false, isValidLongitude("t34.12345"));
		assertEquals(false, isValidLongitude("34.s"));
		assertEquals(false, isValidLongitude("++34.123"));
		assertEquals(false, isValidLongitude("+34.1 23"));
		assertEquals(false, isValidLongitude("+181"));
		assertEquals(false, isValidLongitude("181"));
		assertEquals(false, isValidLongitude("181.0"));
		assertEquals(false, isValidLongitude("1001"));
		assertEquals(false, isValidLongitude("+181.123456"));
		assertEquals(false, isValidLongitude("-181.12345"));
		assertEquals(false, isValidLongitude("+3 .908"));
		assertEquals(false, isValidLongitude("+ 4.908"));
		assertEquals(false, isValidLongitude("+34. 908"));
		assertEquals(false, isValidLongitude("+.908"));
		assertEquals(false, isValidLongitude(".908"));
		assertEquals(false, isValidLongitude("-180.1"));
		assertEquals(false, isValidLongitude("+180.01"));
		assertEquals(false, isValidLongitude("+1801"));
		assertEquals(false, isValidLongitude("1801"));
		assertEquals(false, isValidLongitude("180."));
		assertEquals(false, isValidLongitude("+180."));
		assertEquals(false, isValidLongitude("34."));
		assertEquals(false, isValidLongitude("+34."));
	}
	
	/**
	 * @verifies generating string of patient relationships
	 * @see RegistrationAppUiUtils#getPatientRelationships(Person)
	 */
	@Test
	public void getPatientRelationships_shouldGeneratePatientRelationshipDisplayString() throws Exception {
		// replay
		String displayString = new RegistrationAppUiUtils().getPatientRelationships(patient);
		
		// verify
		assertEquals("Person1GivenName Person1FamilyName - Parent, Person2GivenName Person2FamilyName - Uncle, ", displayString);
	}
}
