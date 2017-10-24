package org.openmrs.module.registrationapp.fragment.controller.search;

import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.List;

public class M2SysSearchFragmentController {

    private static final String[] PROPERTIES_FOR_VIEW = { "patient.id", "patient.givenName", "patient.familyName" ,"score" };

    public void controller(FragmentModel model) {
        model.addAttribute("test", "testval");
    }

    public List<SimpleObject> getPatients(@SpringBean("registrationCoreService") RegistrationCoreService registrationCoreService,
                                          UiUtils ui) {
        List<PatientAndMatchQuality> matches = registrationCoreService.findByBiometricMatch(new BiometricSubject());
        return SimpleObject.fromCollection(matches, ui, PROPERTIES_FOR_VIEW);
    }
}
