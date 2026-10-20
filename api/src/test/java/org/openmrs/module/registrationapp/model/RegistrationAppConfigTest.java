package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests converting between json and model object
 */
public class RegistrationAppConfigTest {

    @Test
    public void testParsing() throws Exception {
        RegistrationAppConfig parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("registrationAppConfig.json"), RegistrationAppConfig.class);
        assertThat(parsed.getAfterCreatedUrl(), is("registrationapp/findPatient.page?appId=registrationapp.registerPatient"));
        assertThat(parsed.getPatientDashboardLink(), is("registrationapp/registrationSummary.page"));
        assertTrue(parsed.isAllowRetrospectiveEntry());
        assertTrue(parsed.isAllowUnknownPatients());
        assertTrue(parsed.isAllowManualIdentifier());
        assertThat(parsed.getRegistrationEncounter().getEncounterType(), is("873f968a-73a8-4f9c-ac78-9f4778b751b6"));
        assertThat(parsed.getRegistrationEncounter().getEncounterRole(), is("cbfe0b9d-9923-404c-941b-f048adc8cdc0"));
        assertThat(parsed.getSections().size(), is(1));
        assertThat(parsed.getIdentifierTypesToDisplay().size(), is(1));
        assertThat(parsed.getIdentifierTypesToDisplay().get(0), is("ef5b4631-3f81-4705-8beb-575851fd37ed"));
    }

    @Test
    public void testShowDuplicatePatientSectionIsOffUnlessConfigured() throws Exception {
        RegistrationAppConfig parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("registrationAppConfig.json"), RegistrationAppConfig.class);
        assertFalse(parsed.isShowDuplicatePatientSection());
    }

    @Test
    public void testShowDuplicatePatientSectionIsParsedAndSerialized() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RegistrationAppConfig parsed = mapper.readValue("{\"showDuplicatePatientSection\": true}", RegistrationAppConfig.class);
        assertTrue(parsed.isShowDuplicatePatientSection());

        // the app descriptor of a site is built from this class, so the setting has to make it into the json
        RegistrationAppConfig config = new RegistrationAppConfig();
        config.setShowDuplicatePatientSection(true);
        assertThat(mapper.valueToTree(config).get("showDuplicatePatientSection").getBooleanValue(), is(true));
        assertThat(mapper.valueToTree(new RegistrationAppConfig()).get("showDuplicatePatientSection").getBooleanValue(), is(false));
    }
}
