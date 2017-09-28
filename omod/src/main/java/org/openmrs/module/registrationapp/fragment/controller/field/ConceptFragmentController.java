package org.openmrs.module.registrationapp.fragment.controller.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.LocaleUtility;

public class ConceptFragmentController {

	public void controller(FragmentModel model, @FragmentParam(required = true, value = "conceptId") Integer conceptId) throws Exception {
		
		Concept concept = Context.getConceptService().getConcept(conceptId);
		if(concept == null){
			model.put("conceptAnswers", new ArrayList<Map<String, String>>());
		} else{
			Collection<Map<String, String>> collection = new ArrayList<Map<String, String>>();
			Map<String, String> conceptMap;
			Locale locale = LocaleUtility.getDefaultLocale();
			
			Collection<ConceptAnswer> conceptAnswers = concept.getAnswers();
			
			for (ConceptAnswer conceptAnswer : conceptAnswers) {
				conceptMap = new HashMap<String, String>();
				String fullname = conceptAnswer.getAnswerConcept().getFullySpecifiedName(locale).getName();
				conceptMap.put("label", fullname);
				conceptMap.put("value", fullname);
				collection.add(conceptMap);
			}
			
			model.put("conceptAnswers", collection);
		}
	}
}
