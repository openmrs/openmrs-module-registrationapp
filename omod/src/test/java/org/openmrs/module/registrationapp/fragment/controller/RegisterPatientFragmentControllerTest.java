package org.openmrs.module.registrationapp.fragment.controller;

import org.apache.struts.mock.MockHttpServletRequest;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.registrationcore.RegistrationData;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.validator.PatientValidator;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisterPatientFragmentControllerTest extends BaseModuleWebContextSensitiveTest {

    public static final String ENCOUNTER_TYPE_UUID = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1";
    public static final String ENCOUNTER_ROLE_UUID = "a0b03050-c99b-11e0-9572-0800200c9a66";
    public static final String WEIGHT_CONCEPT_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
    public static final String CIVIL_STATUS_CONCEPT_UUID = "89ca642a-dab6-4f20-b712-e12ca4fc6d36";
    public static final String MARRIED_CONCEPT_UUID = "92afda7c-78c9-47bd-a841-0de0817027d4";
    public static final String OLD_IDENTIFIER_TYPE_UUID = "2f470aa8-1d73-43b7-81b5-01f0c0dfa53c";

    private RegisterPatientFragmentController controller;

    private AppDescriptor app;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Patient patient;
    private PersonName name;
    private PersonAddress address;
    private Location location;

    private UiSessionContext sessionContext;
    private RegistrationCoreService registrationService;
    private UiUtils uiUtils;
    private MockHttpServletRequest request;

    @Autowired
    private MessageSourceService messageSourceService;

    @Autowired
    PatientService patientService;

    @Autowired
    PersonService personService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private ObsService obsService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProviderService providerService;

    @Autowired @Qualifier("adminService")
    AdministrationService administrationService;

    @Autowired
    EmrApiProperties emrApiProperties;

    @Autowired
    private PatientValidator patientValidator;

    @Before
    public void setUp() throws Exception {
        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("sections");
        config.put("afterCreatedUrl", "url.html?patient={{patientId}}");
        config.putArray("afterCreatedActions");
        config.put("allowRetrospectiveEntry", false);

        controller = new RegisterPatientFragmentController();
        app = new AppDescriptor();
        app.setConfig(config);

        location = locationService.getLocation(1);

        patient = new Patient();
        patient.setGender("F");
        patient.addIdentifier(new PatientIdentifier("123456", patientService.getPatientIdentifierType(2), location));

        name = new PersonName();
        name.setGivenName("Given");
        name.setFamilyName("Family");

        address = new PersonAddress();

        administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE,
                OpenmrsConstants.DEFAULT_ADDRESS_TEMPLATE));

        PersonAttributeType pat = new PersonAttributeType();
        pat.setName(EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME);
        pat.setDescription(EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME);
        personService.savePersonAttributeType(pat);

        // Normally RegistrationCoreService.registerPatient calls idgen to generate the patient's identifier, but
        // setting up a generator programmatically causes a lock, and I am having trouble configuring one via xml data
        // sets because of the non-hibernate-mapped next_sequence_value column.
        // To simplify, we just mock that call so it just creates the patient, since we are not interested in testing
        // the internals of RegistrationCoreService here anyway.
        registrationService = mock(RegistrationCoreService.class);
        when(registrationService.registerPatient(patient, null, null, location)).thenAnswer(new Answer<Patient>() {
            @Override
            public Patient answer(InvocationOnMock invocationOnMock) throws Throwable {
                return patientService.savePatient((Patient) invocationOnMock.getArguments()[0]);
            }
        });
        when(registrationService.registerPatient(any(RegistrationData.class))).thenAnswer(new Answer<Patient>() {
            @Override
            public Patient answer(InvocationOnMock invocationOnMock) throws Throwable {
                RegistrationData registrationData = (RegistrationData)invocationOnMock.getArguments()[0];
                return patientService.savePatient(registrationData.getPatient());
            }
        });

        sessionContext = mock(UiSessionContext.class);
        when(sessionContext.getSessionLocation()).thenReturn(location);
        when(sessionContext.getCurrentProvider()).thenReturn(providerService.getProvider(1));

        // there is one call to UiUtils.message whose result we don't care about here (for the flash message)
        uiUtils = mock(UiUtils.class);
        when(uiUtils.message(anyString(), any(Object[].class))).thenReturn("message");

        request = new MockHttpServletRequest();
    }

    @Test
    public void testPostWithObs() throws Exception {
        request.addParameter("obs." + WEIGHT_CONCEPT_UUID, "70"); // this is WEIGHT (KG)

        FragmentActionResult result = controller.submit(sessionContext, app, registrationService,
                patient, name, address, 30, null, null, null, null, request,
                messageSourceService, encounterService, obsService, conceptService, patientService, emrApiProperties,
                patientValidator, uiUtils);

        assertTrue(result instanceof SuccessResult);
        assertThat(((SuccessResult) result).getMessage(), is("url.html?patient=" + patient.getUuid()));
        assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
        assertThat(obsService.getObservationsByPerson(patient).size(), is(1));

        Obs obs = obsService.getObservationsByPerson(patient).get(0);
        assertThat(obs.getConcept().getUuid(), is(WEIGHT_CONCEPT_UUID));
        assertThat(obs.getValueNumeric(), is(70d));
        assertThat(obs.getLocation(), is(location));
        assertNull(obs.getEncounter());
    }

    @Test
    public void testPostWithCodedObs() throws Exception {
        request.addParameter("obs." + CIVIL_STATUS_CONCEPT_UUID, MARRIED_CONCEPT_UUID);

        FragmentActionResult result = controller.submit(sessionContext, app, registrationService,
                patient, name, address, 30, null, null, null, null, request,
                messageSourceService, encounterService, obsService, conceptService, patientService, emrApiProperties,
                patientValidator, uiUtils);

        assertTrue(result instanceof SuccessResult);
        assertThat(((SuccessResult) result).getMessage(), is("url.html?patient=" + patient.getUuid()));
        assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
        assertThat(obsService.getObservationsByPerson(patient).size(), is(1));

        Obs obs = obsService.getObservationsByPerson(patient).get(0);
        assertThat(obs.getConcept().getUuid(), is(CIVIL_STATUS_CONCEPT_UUID));
        assertThat(obs.getValueCoded().getUuid(), is(MARRIED_CONCEPT_UUID));
        assertThat(obs.getLocation(), is(location));
        assertNull(obs.getEncounter());
    }

    @Test
    public void testPostWithEncounterAndObs() throws Exception {
        ObjectNode regEncConfig = app.getConfig().putObject("registrationEncounter");
        regEncConfig.put("encounterType", ENCOUNTER_TYPE_UUID);
        regEncConfig.put("encounterRole", ENCOUNTER_ROLE_UUID);

        request.addParameter("obs." + WEIGHT_CONCEPT_UUID, "70"); // this is WEIGHT (KG)

        FragmentActionResult result = controller.submit(sessionContext, app, registrationService,
                patient, name, address, 30, null, null, null, null, request,
                messageSourceService, encounterService, obsService, conceptService, patientService, emrApiProperties,
                patientValidator, uiUtils);

        assertTrue(result instanceof SuccessResult);
        assertThat(((SuccessResult) result).getMessage(), is("url.html?patient=" + patient.getUuid()));
        assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
        assertThat(obsService.getObservationsByPerson(patient).size(), is(1));

        Encounter enc = encounterService.getEncountersByPatient(patient).get(0);
        assertThat(enc.getEncounterType().getUuid(), is(ENCOUNTER_TYPE_UUID));
        assertThat(enc.getEncounterProviders().size(), is(1));
        assertThat(enc.getEncounterProviders().iterator().next().getEncounterRole().getUuid(), is(ENCOUNTER_ROLE_UUID));

        Obs obs = obsService.getObservationsByPerson(patient).get(0);
        assertThat(obs.getConcept().getUuid(), is(WEIGHT_CONCEPT_UUID));
        assertThat(obs.getValueNumeric(), is(70d));
        assertThat(obs.getEncounter(), is(enc));
    }

    @Test
    public void testWithUnknownPatient() throws Exception {

        name.setFamilyName(null);
        name.setGivenName(null);

        FragmentActionResult result = controller.submit(sessionContext, app, registrationService,
                patient, name, address, 30, null, null, true, null, request,
                messageSourceService, encounterService, obsService, conceptService, patientService, emrApiProperties,
                patientValidator, uiUtils);

        assertTrue(result instanceof SuccessResult);
        assertThat(((SuccessResult) result).getMessage(), is("url.html?patient=" + patient.getUuid()));
        assertThat(patient.getAttribute(emrApiProperties.getUnknownPatientPersonAttributeType()).getValue(), is("true"));
        assertThat(patient.getGivenName(), is("UNKNOWN"));
        assertThat(patient.getFamilyName(), is("UNKNOWN"));
    }

    @Test
    public void testPostWithPatientIdentifier() throws Exception {

        // clear out the identifier we configured in the setup
        patient.setIdentifiers(null);

        // hack, make location not required
        PatientIdentifierType type = patientService.getPatientIdentifierTypeByUuid(OLD_IDENTIFIER_TYPE_UUID);
        type.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
        patientService.savePatientIdentifierType(type);

        ObjectNode section = objectMapper.createObjectNode();
        ArrayNode questions = objectMapper.createArrayNode();
        ObjectNode question  = objectMapper.createObjectNode();
        ArrayNode fields = objectMapper.createArrayNode();
        ObjectNode identifierField = objectMapper.createObjectNode();
        ObjectNode widget = objectMapper.createObjectNode();

        widget.put("providerName", "test");
        widget.put("fragmentId", "test");

        identifierField.put("type", "patientIdentifier");
        identifierField.put("uuid", OLD_IDENTIFIER_TYPE_UUID);
        identifierField.put("formFieldName", "patientIdentifierField");
        identifierField.put("widget", widget);
        fields.add(identifierField);

        question.put("fields", fields);
        questions.add(question);
        section.put("questions", questions);

        ((ArrayNode) app.getConfig().get("sections")).add(section);

        request.addParameter("patientIdentifierField", "123abcd");

        FragmentActionResult result = controller.submit(sessionContext, app, registrationService,
                patient, name, address, 30, null, null, true, null, request,
                messageSourceService, encounterService, obsService, conceptService, patientService, emrApiProperties,
                patientValidator, uiUtils);

        assertTrue(result instanceof SuccessResult);
        assertThat(((SuccessResult) result).getMessage(), is("url.html?patient=" + patient.getUuid()));
        assertThat(patient.getActiveIdentifiers().size(), is(1));
        assertThat(patient.getActiveIdentifiers().iterator().next().getIdentifier(), is("123abcd"));
        assertThat(patient.getActiveIdentifiers().iterator().next().getIdentifierType().getUuid(), is(OLD_IDENTIFIER_TYPE_UUID));

    }

    @Test
    public void testPostToUpdatePatientIdentifier() throws Exception {

        // hack, make location not required
        PatientIdentifierType type = patientService.getPatientIdentifierTypeByUuid(OLD_IDENTIFIER_TYPE_UUID);
        type.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
        patientService.savePatientIdentifierType(type);

        ObjectNode section = objectMapper.createObjectNode();
        ArrayNode questions = objectMapper.createArrayNode();
        ObjectNode question  = objectMapper.createObjectNode();
        ArrayNode fields = objectMapper.createArrayNode();
        ObjectNode identifierField = objectMapper.createObjectNode();
        ObjectNode widget = objectMapper.createObjectNode();

        widget.put("providerName", "test");
        widget.put("fragmentId", "test");

        identifierField.put("type", "patientIdentifier");
        identifierField.put("uuid", OLD_IDENTIFIER_TYPE_UUID);
        identifierField.put("formFieldName", "patientIdentifierField");
        identifierField.put("widget", widget);
        fields.add(identifierField);

        question.put("fields", fields);
        questions.add(question);
        section.put("questions", questions);

        ((ArrayNode) app.getConfig().get("sections")).add(section);

        request.addParameter("patientIdentifierField", "123abcd");

        FragmentActionResult result = controller.submit(sessionContext, app, registrationService,
                patient, name, address, 30, null, null, true, null, request,
                messageSourceService, encounterService, obsService, conceptService, patientService, emrApiProperties,
                patientValidator, uiUtils);

        assertTrue(result instanceof SuccessResult);
        assertThat(((SuccessResult) result).getMessage(), is("url.html?patient=" + patient.getUuid()));
        assertThat(patient.getActiveIdentifiers().size(), is(1));
        assertThat(patient.getActiveIdentifiers().iterator().next().getIdentifier(), is("123abcd"));
        assertThat(patient.getActiveIdentifiers().iterator().next().getIdentifierType().getUuid(), is(OLD_IDENTIFIER_TYPE_UUID));

    }

}