package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests converting between json and model object
 */
public class TextFieldWidgetTest {

    @Test
    public void testParsing() throws Exception {
        TextFieldWidget parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("textField.json"), TextFieldWidget.class);
        assertThat(parsed.getProviderName(), is("uicommons"));
        assertThat(parsed.getFragmentId(), is("field/text"));
        assertThat(parsed.getConfig().getSize(), is(20));
    }

}
