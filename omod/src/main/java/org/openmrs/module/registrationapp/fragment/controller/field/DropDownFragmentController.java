package org.openmrs.module.registrationapp.fragment.controller.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

public class DropDownFragmentController {
	
	private static String OPTION_LABEL = "label";

    public void controller(FragmentConfiguration config) throws Exception {
        if (config.getAttribute("conceptSet") != null) {
            List < Map < String, Object >> options = (List<Map<String, Object>>) config.getAttribute("options");
            if (options == null) {
            	options = new ArrayList < Map < String, Object >> ();
            }
            String conceptSetUuid = (String) config.getAttribute("conceptSet");
            Concept conceptSet = Context.getConceptService().getConceptByUuid(conceptSetUuid);

            for (Concept concept: conceptSet.getSetMembers()) {
            	Map < String, Object > option = new HashMap < String, Object > ();
                option = new HashMap < String, Object > ();
                option.put("value", concept.getId().toString());
                option.put(OPTION_LABEL, concept.getName(Context.getUserContext().getLocale()).getName());
                options.add(option);
            }
            Collections.sort(options, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                    return ((String) m1.get(OPTION_LABEL)).compareTo((String) m2.get(OPTION_LABEL));
                }
            });
            config.addAttribute("options", options);
        }
    }
}