package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests converting between json and model object
 */
public class TextAreaWidgetTest {

    @Test
    public void testParsing() throws Exception {
        TextAreaWidget parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("textArea.json"), TextAreaWidget.class);
        assertThat(parsed.getProviderName(), is("uicommons"));
        assertThat(parsed.getFragmentId(), is("field/textarea"));
        assertThat(parsed.getConfig().getMaxlength(), is(60));
    }

}
