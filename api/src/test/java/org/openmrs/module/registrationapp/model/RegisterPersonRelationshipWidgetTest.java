package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RegisterPersonRelationshipWidgetTest {

    @Test
    public void testParsing() throws Exception {
        RegisterPersonRelationshipWidget parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("registerPersonRelationship.json"), RegisterPersonRelationshipWidget.class);
        assertThat(parsed.getProviderName(), is("registrationapp"));
        assertThat(parsed.getFragmentId(), is("field/registerPersonRelationship"));
        assertThat(parsed.getConfig().getRelationshipType(), is("8d91a210-c2cc-11de-8d13-0010c6dffd0f"));
        assertThat(parsed.getConfig().getGender(), is("F"));
        assertThat(parsed.getConfig().getMultipleValues(), is(false));
    }
}
