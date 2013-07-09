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
import javax.servlet.http.HttpSession;

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
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.validator.PatientValidator;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, InfoErrorMessageUtil.class })
public class EditPatientDemographicsPageControllerTest {
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private AdministrationService adminService;
	
	private EditPatientDemographicsPageController controller;
	
	@Mock
	private UiUtils ui;
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private PatientValidator patientValidator;
	
	@Mock
	private UiSessionContext uiSessionContext;
	
	@Before
	public void setUpMockUserContext() throws Exception {
		UserContext userContext = Mockito.mock(UserContext.class);
		Context.setUserContext(userContext);
		Mockito.when(userContext.isAuthenticated()).thenReturn(true);
		
		PowerMockito.mockStatic(Context.class);
		Mockito.when(Context.getAdministrationService()).thenReturn(adminService);
		Mockito.when(Context.getPatientService()).thenReturn(patientService);
		
		PowerMockito.spy(InfoErrorMessageUtil.class);
		PowerMockito.doNothing().when(InfoErrorMessageUtil.class);
		InfoErrorMessageUtil.flashInfoMessage(Mockito.any(HttpSession.class), Mockito.anyString());
		
		controller = new EditPatientDemographicsPageController();
	}
	
	/**
	 * @verifies void the old person name and replace it with a new one when it is edited
	 * @see EditPatientDemographicsPageController#post(org.openmrs.module.appui.UiSessionContext,
	 *      org.openmrs.ui.framework.page.PageModel, org.openmrs.api.PatientService,
	 *      org.openmrs.Patient, org.openmrs.PersonName, org.openmrs.layout.web.name.NameTemplate,
	 *      javax.servlet.http.HttpServletRequest, org.openmrs.messagesource.MessageSourceService,
	 *      org.openmrs.ui.framework.session.Session, org.openmrs.validator.PatientValidator,
	 *      org.openmrs.ui.framework.UiUtils)
	 */
	@Test
	public void post_shouldVoidTheOldPersonNameAndReplaceItWithANewOneWhenItIsEdited() throws Exception {
		final String familyName = "familyName";
		PersonName oldName = new PersonName("oldGivenName", null, familyName);
		Patient patient = new Patient();
		patient.addName(oldName);
		
		PersonName newName = new PersonName("newGivenName", null, familyName);
		controller.post(uiSessionContext, null, patientService, patient, newName, null, request, null, null,
		    patientValidator, ui);
		
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
		PersonName newName = new PersonName("givenName", null, "familyName");
		controller.post(uiSessionContext, null, patientService, patient, newName, null, request, null, null,
		    patientValidator, ui);
		
		Assert.assertSame(oldName, patient.getPersonName());
		Assert.assertEquals(false, oldName.isVoided());
		Assert.assertEquals(1, patient.getNames().size());
	}
}
