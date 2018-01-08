package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class PersonAttributeFragmentController {

	public void controller(FragmentModel model, @FragmentParam(required = true, value = "uuid") String uuid) throws Exception {
		
		PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(uuid);
		
		if(personAttributeType.getFormat().equals(Concept.class.getName())){
			int foreignKey = personAttributeType.getForeignKey();
			model.put("foreignKey", foreignKey);
			model.put("codedPersonAttribute", true);
		} else{
			model.put("codedPersonAttribute", false);
		}
	}
}
