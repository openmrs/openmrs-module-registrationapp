package org.openmrs.module.registrationapp.fragment.controller;

import java.util.Collection;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class ConceptFragmentController {

	public void controller(FragmentModel model, @FragmentParam(required = true, value = "uuid") String uuid) {
		Collection<ConceptAnswer> conceptAnswers = null;
		PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(uuid);
		int foreignKey = 0;
		if (personAttributeType != null) {

			foreignKey = personAttributeType.getForeignKey();

		}

		List<Concept> allConcepts = Context.getConceptService().getAllConcepts();
		for (Concept concept : allConcepts) {
			if ((concept.getConceptId()).equals(foreignKey)) {

				conceptAnswers = concept.getAnswers();

			}

		}

		model.addAttribute("conceptAnswers", conceptAnswers);

	}

}
