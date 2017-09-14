package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SectionTest {

    @Test
    public void testParsing() throws Exception {
        Section parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("section.json"), Section.class);
        assertThat(parsed.getId(), is("contactInfo"));
        assertThat(parsed.getLabel(), is("emr.patientDashBoard.contactinfo"));
        assertTrue(parsed.getSkipConfirmation());
        assertThat(parsed.getQuestions().size(), is(1));
        assertThat(parsed.getQuestions().get(0).getLegend(), is("emr.person.telephoneNumber"));
        assertThat(parsed.getQuestions().get(0).getFields().size(), is(1));
        assertThat(parsed.getQuestions().get(0).getFields().get(0).getType(), is("personAttribute"));
        assertThat(parsed.getQuestions().get(0).getFields().get(0).getLabel(), is("registrationapp.patient.phone.label"));
        assertThat(parsed.getQuestions().get(0).getFields().get(0).getFormFieldName(), is("phoneNumber"));
        assertThat(parsed.getQuestions().get(0).getFields().get(0).getUuid(), is("14d4f066-15f5-102d-96e4-000c29c2a5d7"));
        assertThat(parsed.getQuestions().get(0).getFields().get(0).getWidget().get("providerName").getTextValue(), is("uicommons"));
        assertThat(parsed.getQuestions().get(0).getFields().get(0).getWidget().get("fragmentId").getTextValue(), is("field/text"));
    }

}
