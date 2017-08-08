package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;

import java.util.List;

/**
 * Created by user on 02.08.17.
 */
public class FingerprintM2sysFragmentController {

    public void controller() {

    }

    public SimpleObject enroll(@SpringBean("registrationCoreService") RegistrationCoreService service) {
        BiometricEngine biometricEngine = service.getBiometricEngine();
        BiometricSubject biometricSubject = new BiometricSubject();
        BiometricSubject response = biometricEngine.enroll(biometricSubject);

        SimpleObject result = new SimpleObject();
        result.put("id",response.getSubjectId());
        return result;
    }
}
