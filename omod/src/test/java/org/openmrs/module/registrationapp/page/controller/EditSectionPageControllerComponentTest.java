package org.openmrs.module.registrationapp.page.controller;

import org.apache.struts.mock.MockHttpServletRequest;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.validator.PatientValidator;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EditSectionPageControllerComponentTest extends BaseModuleWebContextSensitiveTest {

    public static final String OLD_IDENTIFIER_TYPE_UUID = "2f470aa8-1d73-43b7-81b5-01f0c0dfa53c";

    private EditSectionPageController controller;

    private AppDescriptor app;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Patient patient;

    private Location location;

    private UiSessionContext sessionContext;

    private UiUtils uiUtils;

    private MockHttpServletRequest request;

    private PageModel model;

    private Session session;

    @Autowired
    private MessageSourceService messageSourceService;

    @Autowired
    PatientService patientService;

    @Autowired
    PersonService personService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProviderService providerService;

    @Autowired @Qualifier("adminService")
    private AdministrationService administrationService;

    @Autowired
    private RegistrationCoreService registrationCoreService;

    @Autowired
    EmrApiProperties emrApiProperties;

    @Autowired
    private PatientValidator patientValidator;

    @Before
    public void setUp() throws Exception {

        ObjectNode config = objectMapper.createObjectNode();
        config.putArray("sections");

        controller = new EditSectionPageController();
        app = new AppDescriptor();
        app.setConfig(config);

        location = locationService.getLocation(1);
        patient = patientService.getPatient(2);

        administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE,
                OpenmrsConstants.DEFAULT_ADDRESS_TEMPLATE));

        sessionContext = mock(UiSessionContext.class);
        when(sessionContext.getSessionLocation()).thenReturn(location);
        when(sessionContext.getCurrentProvider()).thenReturn(providerService.getProvider(1));

        // there is one call to UiUtils.message whose result we don't care about here (for the flash message)
        uiUtils = mock(UiUtils.class);
        when(uiUtils.message(anyString(), any(Object[].class))).thenReturn("message");

        model = new PageModel();
        session = mock(Session.class);
        request = new MockHttpServletRequest();
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
        section.put("id", "mainSection");

        ((ArrayNode) app.getConfig().get("sections")).add(section);

        request.addParameter("patientIdentifierField", "123abcd");

        String result = controller.post(sessionContext, model, patient, null, null, 30, 0, app, "mainSection", "successUrl",
                patientService, personService, registrationCoreService, administrationService, request, messageSourceService, session, patientValidator, uiUtils);

        PatientIdentifierType pit = patientService.getPatientIdentifierTypeByUuid(OLD_IDENTIFIER_TYPE_UUID);

        assertThat(result, is("redirect:successUrl"));
        assertThat(patient.getActiveIdentifiers().size(), is(2));  // should only be two identifiers, because patient has two in the test dataset, but the one we add should replace one of these
        assertThat(patient.getPatientIdentifier(pit).getIdentifier(), is("123abcd"));

    }

}
