package org.openmrs.module.registrationapp.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class PossibleDuplicatesWidgetTest {

    @Test
    public void testSerializing() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        PossibleDuplicatesWidget widget = new PossibleDuplicatesWidget();
        widget.getConfig().setTriggerField("phoneNumber");
        ObjectNode json = mapper.convertValue(widget, ObjectNode.class);
        assertThat(json.get("providerName").getTextValue(), is("registrationapp"));
        assertThat(json.get("fragmentId").getTextValue(), is("field/possibleDuplicates"));
        assertThat(json.get("config").get("triggerField").getTextValue(), is("phoneNumber"));

        // the trigger is optional
        ObjectNode withoutTrigger = mapper.convertValue(new PossibleDuplicatesWidget(), ObjectNode.class);
        assertFalse(withoutTrigger.get("config").has("triggerField"));
    }
}
