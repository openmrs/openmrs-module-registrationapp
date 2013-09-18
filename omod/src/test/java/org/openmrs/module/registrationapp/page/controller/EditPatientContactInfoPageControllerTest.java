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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.validator.PatientValidator;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.validation.BindingResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, RegistrationAppUiUtils.class, InfoErrorMessageUtil.class })
public class EditPatientContactInfoPageControllerTest {
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private AdministrationService adminService;
	
	private EditPatientContactInfoPageController controller;
	
	@Mock
	private PatientValidator patientValidator;
	
	@Mock
	private UiUtils ui;
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private UiSessionContext uiSessionContext;
	
	@Mock
	private AppDescriptor app;
	
	@Mock
	private ObjectNode objectNode;
	
	@Before
	public void setup() throws Exception {
		UserContext userContext = Mockito.mock(UserContext.class);
		Context.setUserContext(userContext);
		Mockito.when(userContext.isAuthenticated()).thenReturn(true);
		
		PowerMockito.mockStatic(Context.class);
		Mockito.when(Context.getAdministrationService()).thenReturn(adminService);
		Mockito.when(Context.getPatientService()).thenReturn(patientService);
		
		PowerMockito.spy(RegistrationAppUiUtils.class);
		PowerMockito.doNothing().when(RegistrationAppUiUtils.class);
		RegistrationAppUiUtils.validateLatitudeAndLongitudeIfNecessary(Mockito.any(PersonAddress.class),
		    Mockito.any(BindingResult.class));
		
		PowerMockito.spy(InfoErrorMessageUtil.class);
		PowerMockito.doNothing().when(InfoErrorMessageUtil.class);
		InfoErrorMessageUtil.flashInfoMessage(Mockito.any(HttpSession.class), Mockito.anyString());
		
		ObjectNode objectNode = new ObjectNode(null);
		objectNode.put("sections", new ArrayNode(null));
		Mockito.when(app.getConfig()).thenReturn(objectNode);
		
		controller = new EditPatientContactInfoPageController();
	}
	
	/**
	 * @verifies void the old person address and replace it with a new one when it is edited
	 * @see EditPatientContactInfoPageController#post(org.openmrs.module.appui.UiSessionContext,
	 *      org.openmrs.ui.framework.page.PageModel, org.openmrs.Patient, org.openmrs.PersonAddress,
	 *      org.openmrs.api.PatientService, org.openmrs.module.appframework.domain.AppDescriptor,
	 *      org.openmrs.api.AdministrationService, javax.servlet.http.HttpServletRequest,
	 *      org.openmrs.messagesource.MessageSourceService,
	 *      org.openmrs.ui.framework.session.Session, org.openmrs.validator.PatientValidator,
	 *      org.openmrs.ui.framework.UiUtils)
	 */
	@Test
	public void post_shouldVoidTheOldPersonAddressAndReplaceItWithANewOneWhenItIsEdited() throws Exception {
		PersonAddress address = new PersonAddress();
		address.setCountry("USA");
		Patient patient = new Patient();
		patient.addAddress(address);
		PersonAddress newAddress = new PersonAddress();
		final String newCountry = "Uganda";
		newAddress.setCountry(newCountry);
		
		controller.post(uiSessionContext, null, patient, newAddress, patientService, app, "", null, request, null, null,
		    patientValidator, ui);
		assertSame(newAddress, patient.getPersonAddress());
		assertEquals(newCountry, patient.getPersonAddress().getCountry());
		assertEquals(true, address.isVoided());
		assertEquals(2, patient.getAddresses().size());
	}
	
	/**
	 * @verifies not void the existing address if there are no changes
	 * @see EditPatientContactInfoPageController#post(org.openmrs.module.appui.UiSessionContext,
	 *      org.openmrs.ui.framework.page.PageModel, org.openmrs.Patient, org.openmrs.PersonAddress,
	 *      org.openmrs.api.PatientService, org.openmrs.module.appframework.domain.AppDescriptor,
	 *      org.openmrs.api.AdministrationService, javax.servlet.http.HttpServletRequest,
	 *      org.openmrs.messagesource.MessageSourceService,
	 *      org.openmrs.ui.framework.session.Session, org.openmrs.validator.PatientValidator,
	 *      org.openmrs.ui.framework.UiUtils)
	 */
	@Test
	public void post_shouldNotVoidTheExistingAddressIfThereAreNoChanges() throws Exception {
		PersonAddress address = new PersonAddress();
		String country = "Uganda";
		address.setCountry(country);
		Patient patient = new Patient();
		patient.addAddress(address);
		PersonAddress newAddress = new PersonAddress();
		newAddress.setCountry(country);
		
		controller.post(uiSessionContext, null, patient, newAddress, patientService, app, "", null, request, null, null,
		    patientValidator, ui);
		
		assertSame(address, patient.getPersonAddress());
		assertEquals(false, address.isVoided());
		assertEquals(1, patient.getAddresses().size());
	}
}
