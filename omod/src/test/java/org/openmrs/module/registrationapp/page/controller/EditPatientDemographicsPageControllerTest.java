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
package org.openmrs.module.registrationapp.page.controller;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.layout.web.name.NameTemplate;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.openmrs.validator.PatientValidator;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class EditPatientDemographicsPageControllerTest {
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private AdministrationService adminService;
	
	private EditPatientDemographicsPageController controller;
	
	@Mock
	private UiUtils ui;
	
	private HttpServletRequest request;
	
	@Mock
	private PatientValidator patientValidator;
	
	@Mock
	private UiSessionContext uiSessionContext;
	
	private PageModel model;
	
	@Before
	public void setUpMockUserContext() throws Exception {
		UserContext userContext = Mockito.mock(UserContext.class);
		Context.setUserContext(userContext);
		Mockito.when(userContext.isAuthenticated()).thenReturn(true);
		
		PowerMockito.mockStatic(Context.class);
		Mockito.when(Context.getPatientService()).thenReturn(patientService);
		Mockito.when(Context.getAdministrationService()).thenReturn(adminService);
		
		controller = new EditPatientDemographicsPageController();
		request = new MockHttpServletRequest();
		model = new PageModel();
		Mockito.when(ui.message(Mockito.anyString())).thenReturn("Testing message");
	}
	
	/**
	 * @see EditPatientDemographicsPageController#post(UiSessionContext,PageModel,PatientService,Patient,PersonName,NameTemplate,HttpServletRequest,MessageSourceService,Session,PatientValidator,UiUtils)
	 * @verifies void the old person name and replace it with a new one when the given name is
	 *           changed
	 */
	@Test
	public void post_shouldVoidTheOldPersonNameAndReplaceItWithANewOneWhenTheGivenNameIsChanged() throws Exception {
		final String familyName = "familyName";
		PersonName oldName = new PersonName("oldGivenName", null, familyName);
		Patient patient = new Patient();
		patient.addName(oldName);
		
		PersonName newName = new PersonName("newGivenName", null, familyName);
		controller.post(uiSessionContext, model, null, patient, newName, null, request, null, null, patientValidator, ui);
		
		Assert.assertNotSame(oldName, patient.getPersonName());
		Assert.assertSame(newName, patient.getPersonName());
		Assert.assertEquals(true, oldName.isVoided());
		Assert.assertEquals(2, patient.getNames().size());
	}
	
	/**
	 * @see EditPatientDemographicsPageController#post(UiSessionContext,PageModel,PatientService,Patient,PersonName,NameTemplate,HttpServletRequest,MessageSourceService,Session,PatientValidator,UiUtils)
	 * @verifies void the old person name and replace it with a new one when the family name is
	 *           changed
	 */
	@Test
	public void post_shouldVoidTheOldPersonNameAndReplaceItWithANewOneWhenTheFamilyNameIsChanged() throws Exception {
		final String giveName = "giveName";
		PersonName oldName = new PersonName(giveName, null, "oldFamilyName");
		Patient patient = new Patient();
		patient.addName(oldName);
		
		PersonName newName = new PersonName(giveName, null, "newFamilyName");
		controller.post(uiSessionContext, model, null, patient, newName, null, request, null, null, patientValidator, ui);
		
		Assert.assertNotSame(oldName, patient.getPersonName());
		Assert.assertSame(newName, patient.getPersonName());
		Assert.assertEquals(true, oldName.isVoided());
		Assert.assertEquals(2, patient.getNames().size());
	}
	
	/**
	 * @see EditPatientDemographicsPageController#post(org.openmrs.module.appui.UiSessionContext,
	 *      org.openmrs.ui.framework.page.PageModel, org.openmrs.api.PatientService,
	 *      org.openmrs.Patient, org.openmrs.PersonName, org.openmrs.layout.web.name.NameTemplate,
	 *      javax.servlet.http.HttpServletRequest, org.openmrs.messagesource.MessageSourceService,
	 *      org.openmrs.ui.framework.session.Session, org.openmrs.validator.PatientValidator,
	 *      org.openmrs.ui.framework.UiUtils)
	 * @verifies not void the existing name if there are no changes in the name
	 */
	@Test
	public void post_shouldNotVoidTheExistingNameIfThereAreNoChangesInTheName() throws Exception {
		PersonName oldName = new PersonName("givenName", null, "familyName");
		Patient patient = new Patient();
		patient.addName(oldName);
		
		//should be case insensitive
		PersonName newName = new PersonName("GivenName", null, "FamilyName");
		controller.post(uiSessionContext, model, null, patient, newName, null, request, null, null, patientValidator, ui);
		
		Assert.assertSame(oldName, patient.getPersonName());
		Assert.assertEquals(false, oldName.isVoided());
		Assert.assertEquals(1, patient.getNames().size());
	}
}
