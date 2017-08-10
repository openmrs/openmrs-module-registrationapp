package org.openmrs.module.registrationapp.fragment.controller.field;

import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

public class FingerprintM2sysFragmentController {

    private BiometricEngine biometricEngine;

    public FingerprintM2sysFragmentController() {
        biometricEngine = Context.getService(RegistrationCoreService.class).getBiometricEngine();
    }

    public void controller() { }

    public SimpleObject enroll() {
        if (!isBiometricEngineEnable()) {
            return null;
        }
        BiometricSubject response = biometricEngine.enroll(null);

        SimpleObject result = new SimpleObject();
        result.put("id", response.getSubjectId());
        return result;
    }

    public SimpleObject getStatus() {
        if (!isBiometricEngineEnable()) {
            return null;
        }
        BiometricEngineStatus response = biometricEngine.getStatus();

        SimpleObject result = new SimpleObject();
        result.put("enabled", response.isEnabled());
        result.put("statusMessage", response.getStatusMessage());
        result.put("description", response.getDescription());

        return result;
    }

    public SimpleObject update(@RequestParam("id") String id) {
        if (!isBiometricEngineEnable()) {
            return null;
        }
        BiometricSubject biometricSubject = new BiometricSubject();
        biometricSubject.setSubjectId(id);
        BiometricSubject response = biometricEngine.update(biometricSubject);

        SimpleObject result = new SimpleObject();
        result.put("id", response.getSubjectId());
        return result;
    }

    public SimpleObject updateSubjectId(@RequestParam("oldId") String oldId,
                                        @RequestParam("newId") String newId) {
        if (!isBiometricEngineEnable()) {
            return null;
        }
        BiometricSubject response = biometricEngine.updateSubjectId(oldId, newId);

        SimpleObject result = new SimpleObject();
        result.put("id", response.getSubjectId());
        return result;
    }

    public List<SimpleObject> search(@RequestParam("id") String id) {
        if (!isBiometricEngineEnable()) {
            return null;
        }
        BiometricSubject biometricSubject = new BiometricSubject();
        biometricSubject.setSubjectId(id);
        List<BiometricMatch> response = biometricEngine.search(biometricSubject);

        return toSimpleObjectList(response);
    }

    public SimpleObject lookup(@RequestParam("id") String id) {
        if (!isBiometricEngineEnable()) {
            return null;
        }
        BiometricSubject response = biometricEngine.lookup(id);

        SimpleObject result = new SimpleObject();
        result.put("id", response.getSubjectId());
        return result;
    }

    public void delete(@RequestParam("id") String id) {
        if (!isBiometricEngineEnable()) {
            return;
        }
        biometricEngine.delete(id);
    }

    private List<SimpleObject> toSimpleObjectList(List<BiometricMatch> matches) {
        List<SimpleObject> resultList = new ArrayList<SimpleObject>();
        for(BiometricMatch match : matches) {
            SimpleObject simpleObject = new SimpleObject();
            simpleObject.put("subjectId", match.getSubjectId());
            simpleObject.put("matchScore", match.getMatchScore());
            resultList.add(simpleObject);
        }
        return resultList;
    }

    private boolean isBiometricEngineEnable() {
        return getBiometricEngine() != null;
    }

    public BiometricEngine getBiometricEngine() {
        return biometricEngine;
    }

    public void setBiometricEngine(BiometricEngine biometricEngine) {
        this.biometricEngine = biometricEngine;
    }
}
