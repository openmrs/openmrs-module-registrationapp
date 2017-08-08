package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 02.08.17.
 */
public class FingerprintM2sysFragmentController {

    private static BiometricEngine biometricEngine;

    public void controller(@SpringBean("registrationCoreService") RegistrationCoreService service) {
        biometricEngine = service.getBiometricEngine();
    }

    public SimpleObject enroll() {
        BiometricSubject biometricSubject = new BiometricSubject();
        BiometricSubject response = biometricEngine.enroll(biometricSubject);

        SimpleObject result = new SimpleObject();
        result.put("id",response.getSubjectId());
        return result;
    }

    public SimpleObject getStatus() {
        BiometricEngineStatus response = biometricEngine.getStatus();

        SimpleObject result = new SimpleObject();
        result.put("enabled", response.isEnabled());
        result.put("statusMessage", response.getStatusMessage());
        result.put("description", response.getDescription());

        return result;
    }

    public SimpleObject update(@RequestParam("id") String id) {
        BiometricSubject biometricSubject = new BiometricSubject();
        biometricSubject.setSubjectId(id);
        BiometricSubject response = biometricEngine.update(biometricSubject);

        SimpleObject result = new SimpleObject();
        result.put("id",response.getSubjectId());
        return result;
    }

    public SimpleObject updateSubjectId(@RequestParam("oldId") String oldId,
                                        @RequestParam("newId") String newId) {
        BiometricSubject response = biometricEngine.updateSubjectId(oldId, newId);

        SimpleObject result = new SimpleObject();
        result.put("id",response.getSubjectId());
        return result;
    }

    public List<SimpleObject> search(@RequestParam("id") String id) {
        BiometricSubject biometricSubject = new BiometricSubject();
        biometricSubject.setSubjectId(id);
        List<BiometricMatch> response = biometricEngine.search(biometricSubject);

        return toSimpleObjectList(response);
    }

    public SimpleObject lookup(@RequestParam("id") String id) {
        BiometricSubject response = biometricEngine.lookup(id);

        SimpleObject result = new SimpleObject();
        result.put("id",response.getSubjectId());
        return result;
    }

    public void delete(@RequestParam("id") String id) {
        biometricEngine.delete(id);
    }

    private List<SimpleObject> toSimpleObjectList(List<BiometricMatch> matches) {
        List<SimpleObject> resultList = new ArrayList<SimpleObject>();
        for(BiometricMatch match : matches) {
            SimpleObject simpleObject = new SimpleObject();
            simpleObject.put("subjectId",match.getSubjectId());
            simpleObject.put("matchScore",match.getMatchScore());
            resultList.add(simpleObject);
        }
        return resultList;
    }
}
