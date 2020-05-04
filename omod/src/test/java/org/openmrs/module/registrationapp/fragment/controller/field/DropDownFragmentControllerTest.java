package org.openmrs.module.registrationapp.fragment.controller.field;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class DropDownFragmentControllerTest {
	
	private static String CONCEPT_SET_UUID = "concept-set-uuid";
	
	private DropDownFragmentController dwController;
	
	@Mock
	private ConceptService conceptService;
	
	@Mock
	private UserContext userContext;
	
	private FragmentConfiguration config;
	
	private Concept conceptSet;
	
	private Concept concept1;
	
	private Concept concept2; 
	
	private ConceptName conceptName1;
	
	private ConceptName conceptName2;
	
	@Before
    public void setUp() throws Exception {
		dwController = new DropDownFragmentController();
		conceptSet = new Concept(0);
		conceptSet.setUuid(CONCEPT_SET_UUID);
		concept1 = new Concept(1);
		concept2 = new Concept(2);
		
		conceptName1 = new ConceptName(1);
		conceptName1.setName("name1");
		conceptName1.setLocale(Locale.ENGLISH);
		conceptName2 = new ConceptName(2);
		conceptName2.setName("name2");
		conceptName2.setLocale(Locale.ENGLISH);
		
		concept1.addName(conceptName1);
		concept2.addName(conceptName2);
		
		conceptSet.addSetMember(concept1);
		conceptSet.addSetMember(concept2);
		
		PowerMockito.mockStatic(Context.class);
		when(Context.getConceptService()).thenReturn(conceptService);
		when(Context.getUserContext()).thenReturn(userContext);
		when(conceptService.getConceptByUuid(CONCEPT_SET_UUID)).thenReturn(conceptSet);
		when(userContext.getLocale()).thenReturn(Locale.ENGLISH);		
    }
    
    @Test
    public void controller_shouldAddOptionsFromConceptSet() throws Exception {    	
    	// setup
    	config = new FragmentConfiguration();
    	config.addAttribute("conceptSet", CONCEPT_SET_UUID);
    	
    	// replay
    	dwController.controller(config);
    	
    	// verify
    	List < Map < String, Object >> options = (List < Map < String, Object >>) config.getAttribute("options");
	 	assertTrue(config.containsKey("options"));
	 	assertThat(options.size(), is(3));
	 	assertThat((String) options.get(0).get("label"), is(""));
	 	assertThat((String) options.get(0).get("value"), is(""));
	 	assertThat((String) options.get(1).get("label"), is("name1"));
	 	assertThat((String) options.get(1).get("value"), is("1"));
	 	assertThat((String) options.get(2).get("label"), is("name2"));
	 	assertThat((String) options.get(2).get("value"), is("2"));
	 	
	 	
    }
}