package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests converting between json and model object
 */
public class PersonAddressWithHierarchyWidgetTest {

    @Test
    public void testParsing() throws Exception {
        PersonAddressWithHierarchyWidget parsed = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("personAddressWithHierarchy.json"), PersonAddressWithHierarchyWidget.class);
        assertThat(parsed.getProviderName(), is("registrationapp"));
        assertThat(parsed.getFragmentId(), is("field/personAddressWithHierarchy"));
        assertThat(parsed.getConfig().getShortcutFor(), is("address1"));
        assertThat(parsed.getConfig().getManualFields().size(), is(1));
        assertThat(parsed.getConfig().getManualFields().get(0), is("address2"));
        assertThat(parsed.getConfig().getFieldMappings().get("address1"), is("obs.CIEL:1234"));
        assertThat(parsed.getConfig().getFieldMappings().get("address2"), is("obs.CIEL:5678"));
    }

}
