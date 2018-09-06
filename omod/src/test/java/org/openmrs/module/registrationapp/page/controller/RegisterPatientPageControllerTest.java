package org.openmrs.module.registrationapp.page.controller;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Relationship;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.registrationapp.AddressSupportCompatibility;
import org.openmrs.module.registrationapp.NameSupportCompatibility;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class})
public class RegisterPatientPageControllerTest {
    @Mock
    private UiSessionContext uiSessionContext;
    
    @Mock
    private PersonService personService;
    
    @Mock
    private AppDescriptor app;

    @Mock
    private UiUtils ui;

    @Mock
    AppFrameworkService appFrameworkService;

    @Mock
    EmrApiProperties emrApiProperties;

    @Mock
    NameSupportCompatibility nameSupportCompatibility;

    @Mock
    AddressSupportCompatibility addressSupport;

    @Mock
    JsonNode jsonNode;

    @Mock
    AdministrationService administrationService;

    private RegisterPatientPageController registerPatientPageController;
    
    PageModel pageModel;
    PatientIdentifierType patientIdentifierType;
    String breadcrumbOverride;

    @Before
    public void setUpMockUserContext() throws Exception {
        PowerMockito.mockStatic(Context.class);
        Mockito.when(nameSupportCompatibility.getDefaultLayoutTemplate()).thenReturn(new Object());
        Mockito.when(Context.getRegisteredComponent(NameSupportCompatibility.ID, NameSupportCompatibility.class)).thenReturn(nameSupportCompatibility);
        Mockito.when(administrationService.getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false")).thenReturn("value");
        Mockito.when(Context.getAdministrationService()).thenReturn(administrationService);
        Mockito.when(personService.getAllRelationships()).thenReturn(new ArrayList<Relationship>());
        Mockito.when(Context.getPersonService()).thenReturn(personService);

        Mockito.when(addressSupport.getDefaultLayoutTemplate()).thenReturn(new Object());
        Mockito.when(Context.getRegisteredComponent(AddressSupportCompatibility.ID, AddressSupportCompatibility.class)).thenReturn(addressSupport);

        UserContext userContext = Mockito.mock(UserContext.class);
        Context.setUserContext(userContext);
        Mockito.when(userContext.isAuthenticated()).thenReturn(true);

        pageModel = new PageModel();

        ObjectNode objectNode = new ObjectNode(null);
        objectNode.put("sections", new ArrayNode(null));
        Mockito.when(jsonNode.isNull()).thenReturn(false);
        Mockito.when(jsonNode.getBooleanValue()).thenReturn(true);
        objectNode.put("registrationEncounter", jsonNode);
        objectNode.put("allowRetrospectiveEntry", jsonNode);
        objectNode.put("allowUnknownPatients", jsonNode);
        objectNode.put("allowManualIdentifier", jsonNode);
        objectNode.put("patientDashboardLink", jsonNode);
        Mockito.when(app.getConfig()).thenReturn(objectNode);

        Mockito.when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(patientIdentifierType);

        patientIdentifierType = new PatientIdentifierType();
        patientIdentifierType.setId(1);

        breadcrumbOverride = "home>sample";

        List<Extension> extensions = new ArrayList<Extension>();
        extensions.add(new Extension());
        Mockito.when(appFrameworkService.getExtensionsForCurrentUser("registerPatient.includeFragments")).thenReturn(extensions);

        registerPatientPageController = new RegisterPatientPageController();
    }

    @Test
    public void get_shouldCreateANewPatientWhenPatientIsNull() throws Exception {
        // Fixture setup
        Patient existingPatient = null;

        // Execution
        registerPatientPageController.get(
                uiSessionContext,
                pageModel,
                app,
                existingPatient,
                breadcrumbOverride,
                existingPatient,
                emrApiProperties,
                appFrameworkService,
                ui);

        // Assertion
        Assert.assertNotNull(pageModel.getAttribute("patient"));
    }

    @Test
    public void get_shouldUseExistingPatientWhenPatientIsNotNull() throws Exception {
        // Fixture setup
        Patient existingPatient = new Patient();
        existingPatient.setId(10);

        // Execution
        registerPatientPageController.get(
                uiSessionContext,
                pageModel,
                app,
                existingPatient,
                breadcrumbOverride,
                existingPatient,
                emrApiProperties,
                appFrameworkService,
                ui);

        // Assertion
        Patient result = (Patient) pageModel.getAttribute("patient");
        Assert.assertEquals(existingPatient.getId(), result.getId());
    }
}
