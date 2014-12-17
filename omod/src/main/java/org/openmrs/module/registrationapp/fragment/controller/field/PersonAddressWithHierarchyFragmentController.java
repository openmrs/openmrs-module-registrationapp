package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.layout.web.address.AddressTemplate;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.List;

public class PersonAddressWithHierarchyFragmentController {

    public void controller(FragmentModel model) {
        // there is no accessible spring bean for this, so we fetch it in the old hacky way
        AddressHierarchyService addressHierarchyService = Context.getService(AddressHierarchyService.class);

        List<AddressHierarchyLevel> levels = addressHierarchyService.getOrderedAddressHierarchyLevels();
        model.put("levels", levels);

        AddressTemplate addressTemplate = AddressSupport.getInstance().getDefaultLayoutTemplate();
        model.put("addressTemplate", addressTemplate);
    }

}
