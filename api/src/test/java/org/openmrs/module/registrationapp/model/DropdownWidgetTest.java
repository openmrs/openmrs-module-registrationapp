package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests converting between json and model object
 */
public class DropdownWidgetTest {

    @Test
    public void testParsing() throws Exception {
        DropdownWidget parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("dropdown.json"), DropdownWidget.class);
        assertThat(parsed.getProviderName(), is("uicommons"));
        assertThat(parsed.getFragmentId(), is("field/dropDown"));
        assertTrue(parsed.getConfig().isExpanded());
        assertTrue(parsed.getConfig().isHideEmptyLabel());
        assertThat(parsed.getConfig().getInitialValue(), is("2"));
        assertThat(parsed.getConfig().getOptions().size(), is(3));
        assertThat(parsed.getConfig().getOptions().get(0).getValue(), is("1"));
        assertThat(parsed.getConfig().getOptions().get(0).getLabel(), is("One"));
        assertThat(parsed.getConfig().getOptions().get(1).getValue(), is("2"));
        assertThat(parsed.getConfig().getOptions().get(1).getLabel(), is("Two"));
        assertThat(parsed.getConfig().getOptions().get(2).getValue(), is("3"));
        assertThat(parsed.getConfig().getOptions().get(2).getLabel(), is("Three"));
    }

}
