package org.openmrs.module.registrationapp.fragment.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.search.FieldMatchResult;
import org.openmrs.module.registrationcore.api.search.FieldMatchStatus;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

public class MatchingPatientsFragmentControllerTest {
	
	private AdministrationService adminService = mock(AdministrationService.class);
	
	@Test
	public void getCutoff_shouldReadGlobalProperty() {
		when(adminService.getGlobalProperty("some.gp")).thenReturn(" 7.5 ");
		assertThat(MatchingPatientsFragmentController.getCutoff(adminService, "some.gp", 2.0), is(7.5));
	}
	
	@Test
	public void getCutoff_shouldUseDefaultIfGlobalPropertyIsMissingBlankOrNotANumber() {
		when(adminService.getGlobalProperty("blank.gp")).thenReturn("  ");
		when(adminService.getGlobalProperty("bad.gp")).thenReturn("lots");
		assertThat(MatchingPatientsFragmentController.getCutoff(adminService, "missing.gp", 2.0), is(2.0));
		assertThat(MatchingPatientsFragmentController.getCutoff(adminService, "blank.gp", 2.0), is(2.0));
		assertThat(MatchingPatientsFragmentController.getCutoff(adminService, "bad.gp", 2.0), is(2.0));
	}
	
	@Test
	public void isStrong_shouldIncludeTheCutoffItself() {
		assertThat(MatchingPatientsFragmentController.isStrong(12.0, 12.0), is(true));
		assertThat(MatchingPatientsFragmentController.isStrong(11.9, 12.0), is(false));
		assertThat(MatchingPatientsFragmentController.isStrong(null, 12.0), is(false));
	}
	
	@Test
	public void toSimpleFieldResults_shouldKeepOrderAndLowercaseStatus() {
		List<SimpleObject> simple = MatchingPatientsFragmentController.toSimpleFieldResults(Arrays.asList(
		    new FieldMatchResult(FieldMatchResult.NAME, FieldMatchStatus.SIMILAR, 0.5),
		    new FieldMatchResult(FieldMatchResult.PHONE, FieldMatchStatus.MISSING, 0)));
		
		assertThat(simple.size(), is(2));
		assertThat((String) simple.get(0).get("field"), is("name"));
		assertThat((String) simple.get(0).get("status"), is("similar"));
		assertThat((Double) simple.get(0).get("score"), is(0.5));
		assertThat((String) simple.get(1).get("field"), is("phone"));
		assertThat((String) simple.get(1).get("status"), is("missing"));
	}
	
	@Test
	public void toSimpleFieldResults_shouldBeEmptyWhenAlgorithmReportsNothing() {
		assertThat(MatchingPatientsFragmentController.toSimpleFieldResults(
		    new org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality(new Patient(), 1.0, null)
		            .getFieldResults()).size(), is(0));
	}
	
	@Test
	public void getFormAttributeValues_shouldReturnOnlyPersonAttributeFieldsThatAreOnTheFormAndFilledIn() {
		PersonAttributeType phoneType = new PersonAttributeType();
		phoneType.setUuid("phone-type-uuid");
		PersonAttributeType motherType = new PersonAttributeType();
		motherType.setUuid("mother-type-uuid");
		PersonAttributeType otherType = new PersonAttributeType();
		otherType.setUuid("other-type-uuid");
		
		Patient patient = new Patient();
		patient.addAttribute(new PersonAttribute(phoneType, "555-1234"));
		patient.addAttribute(new PersonAttribute(motherType, "  "));
		patient.addAttribute(new PersonAttribute(otherType, "not on the form"));
		
		NavigableFormStructure form = mock(NavigableFormStructure.class);
		when(form.getFields()).thenReturn(Arrays.asList(
		    field("personAttribute", "phoneNumber", "phone-type-uuid"),
		    field("personAttribute", "mothersFirstName", "mother-type-uuid"),
		    field("personAttribute", "birthplace", "missing-type-uuid"),
		    field("patientIdentifier", "nationalId", "phone-type-uuid")));
		
		Map<String, String> values = MatchingPatientsFragmentController.getFormAttributeValues(form, patient);
		
		assertThat(values.size(), is(1));
		assertThat(values.get("phoneNumber"), is("555-1234"));
	}
	
	@Test
	public void formatBirthdate_shouldFormatTheDateWithTheUiFormatter() {
		UiUtils ui = mock(UiUtils.class);
		when(ui.format(any(Date.class))).thenAnswer(invocation -> new SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
		        .format((Date) invocation.getArguments()[0]));
		
		assertThat(MatchingPatientsFragmentController.formatBirthdate(23, 12, 1991, ui), is("23.Dec.1991"));
	}
	
	@Test
	public void formatBirthdate_shouldBeEmptyForADateThatDoesNotExist() {
		assertThat(MatchingPatientsFragmentController.formatBirthdate(31, 2, 1991, mock(UiUtils.class)), is(""));
	}
	
	private Field field(String type, String formFieldName, String uuid) {
		Field field = new Field();
		field.setType(type);
		field.setFormFieldName(formFieldName);
		field.setUuid(uuid);
		return field;
	}
}
