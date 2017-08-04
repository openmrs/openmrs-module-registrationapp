package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.ui.framework.annotation.SpringBean;

/**
 * Created by user on 02.08.17.
 */
public class FingerprintM2sysFragmentController {

    public void controller() {

    }

    public void enroll(@SpringBean("registrationCoreService") RegistrationCoreService service) {
        BiometricEngine biometricEngine = service.getBiometricEngine();
        biometricEngine.enroll(null);
    }
}
