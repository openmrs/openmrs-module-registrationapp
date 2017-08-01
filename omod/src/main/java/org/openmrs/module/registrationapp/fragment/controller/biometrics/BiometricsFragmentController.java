package org.openmrs.module.registrationapp.fragment.controller.biometrics;

import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.biometrics.model.Fingerprint;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class BiometricsFragmentController {


    public List<SimpleObject> search(@SpringBean("registrationCoreService") RegistrationCoreService service,
            HttpServletRequest request, UiUtils ui) throws Exception {

        List<SimpleObject> results = new ArrayList<SimpleObject>();
        BiometricEngine biometricEngine = service.getBiometricEngine();

        if (biometricEngine != null && biometricEngine.getStatus().isEnabled()) {
            BiometricSubject subject = new BiometricSubject();
            Fingerprint fingerprint = new Fingerprint();
            String fingerprintTemplate  = ((String[]) request.getParameterMap().get("template"))[0];
            fingerprint.setTemplate(fingerprintTemplate);
            subject.addFingerprint(fingerprint);
            List<BiometricMatch> matches = biometricEngine.search(subject);
            for (BiometricMatch match : matches) {
                results.add(SimpleObject.fromObject(match, ui, "subjectId", "matchScore"));
            }
        }
        return results;
    }
}
