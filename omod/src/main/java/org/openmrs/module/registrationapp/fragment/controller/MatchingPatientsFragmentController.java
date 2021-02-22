/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationapp.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
//TODO this class is to complex. Filtering by patient properties should be extracted into separate class.
public class MatchingPatientsFragmentController {

    public static final int MAX_RESULTS = 10;
    public static final double CUTOFF = 2.0;
    private final Log log = LogFactory.getLog(this.getClass());

    public static final String[] PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "gender", "birthdate", "personAddress"};

    public static final String[] MPI_PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "gender", "birthdate", "personAddress", "mpiPatient", "sourceLocation", "mothersName", "phoneNumber"};

    public List<SimpleObject> getSimilarPatients(@RequestParam("appId") AppDescriptor app,
                                                 @SpringBean("registrationCoreService") RegistrationCoreService service,
                                                 @ModelAttribute("patient") @BindParams Patient patient,
                                                 @ModelAttribute("personName") @BindParams PersonName name,
                                                 @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                                 @RequestParam(value = "birthdateYears", required = false) Integer birthdateYears,
                                                 @RequestParam(value = "birthdateMonths", required = false) Integer birthdateMonths,
                                                 HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);

        Map<String, Object> otherDataPoints = createDataPoints(birthdateYears, birthdateMonths);

        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        Map<String, String[]> parameterMap = request.getParameterMap();
        if(parameterMap.get("Telephone Number") != null){
            String phoneNumber = parameterMap.get("Telephone Number")[0];
            if (StringUtils.isNotBlank(phoneNumber)) {
                otherDataPoints.put("phoneNumber",phoneNumber);
            }
        }

        if(parameterMap.get("localBiometricSubjectId") != null){
            String localBiometricSubjectId = parameterMap.get("localBiometricSubjectId")[0];
            if (StringUtils.isNotBlank(localBiometricSubjectId)) {
                otherDataPoints.put("localBiometricSubjectId",localBiometricSubjectId);
            }
        }
        if(parameterMap.get("nationalBiometricSubjectId") != null){
            String nationalBiometricSubjectId = parameterMap.get("nationalBiometricSubjectId")[0];
            if (StringUtils.isNotBlank(nationalBiometricSubjectId)) {
                otherDataPoints.put("nationalBiometricSubjectId",nationalBiometricSubjectId);
            }
        }

        RegisterPatientFormBuilder.resolvePatientIdentifierFields(formStructure, patient, parameterMap);
        List<PatientAndMatchQuality> matches = new ArrayList<PatientAndMatchQuality>();
        List<PatientAndMatchQuality> fastSimilarPatients = service.findFastSimilarPatients(patient, otherDataPoints, CUTOFF, determineMaxResults(app));
        if(fastSimilarPatients!=null && fastSimilarPatients.size() > 0){
            matches.addAll(fastSimilarPatients) ;
        }
        return getSimpleObjects(app, ui, matches);
    }

    public List<SimpleObject> getExactPatients(@RequestParam("appId") AppDescriptor app,
                                               @SpringBean("registrationCoreService") RegistrationCoreService service,
                                               @ModelAttribute("patient") @BindParams Patient patient,
                                               @ModelAttribute("personName") @BindParams PersonName name,
                                               @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                               @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                                               @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                                               HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, app, name, address, request);

        Map<String, Object> otherDataPoints = createDataPoints(birthdateYears, birthdateMonths);

        List<PatientAndMatchQuality> matches = service.findPreciseSimilarPatients(patient, otherDataPoints, CUTOFF, determineMaxResults(app));
        return getSimpleObjects(app, ui, matches);
    }

    public List<SimpleObject> getBiometricMatches(@RequestParam("appId") AppDescriptor app,
                                                  @SpringBean("registrationCoreService") RegistrationCoreService service,
                                                  @SpringBean("patientService") PatientService patientService,
                                                  HttpServletRequest request, UiUtils ui) throws Exception {

        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);
        List<PatientAndMatchQuality> matches = getBiometricMatches(service, patientService, formStructure, request.getParameterMap());
        return getSimpleObjects(app, ui, matches);
    }

    private List<PatientAndMatchQuality> getBiometricMatches(RegistrationCoreService service, PatientService patientService,
                                                             NavigableFormStructure formStructure, Map<String, String[]> parameterMap) {
        List<PatientAndMatchQuality> ret = new ArrayList<PatientAndMatchQuality>();
        BiometricEngine biometricEngine = service.getBiometricEngine();
        if (biometricEngine != null && biometricEngine.getStatus().isEnabled()) {
            Map<Field, BiometricSubject> biometricFields = RegisterPatientFormBuilder.extractBiometricDataFields(formStructure, parameterMap);
            for (Field biometricField : biometricFields.keySet()) {
                BiometricSubject subject = biometricFields.get(biometricField);
                List<BiometricMatch> biometricMatches = biometricEngine.search(subject);
                if (biometricMatches.size() > 0) {
                    List<PatientIdentifierType> biometricIdList = Arrays.asList(patientService.getPatientIdentifierTypeByUuid(biometricField.getUuid()));
                    for (BiometricMatch match : biometricMatches) {
                        List<Patient> patients = patientService.getPatients(null, match.getSubjectId(), biometricIdList, true);
                        for (Patient patient : patients) {
                            ret.add(new PatientAndMatchQuality(patient, match.getMatchScore(), Arrays.asList(biometricField.getFormFieldName())));
                        }
                    }
                }
            }
        }
        return ret;
    }

    private void addToPatient(Patient patient, AppDescriptor app, PersonName name, PersonAddress address, HttpServletRequest request) throws IOException {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        patient.addName(name);
        patient.addAddress(address);

        if (formStructure != null) {
            RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
        }
    }

    private Map<String, Object> createDataPoints(Integer birthdateYears, Integer birthdateMonths) {
        Map<String, Object> otherDataPoints = new HashMap<String, Object>();
        otherDataPoints.put("birthdateYears", birthdateYears);
        otherDataPoints.put("birthdateMonths", birthdateMonths);
        return otherDataPoints;
    }

    private List<SimpleObject> getSimpleObjects(AppDescriptor app, UiUtils ui, List<PatientAndMatchQuality> matches) {
        List<SimpleObject> result = new ArrayList<SimpleObject>();

        for (PatientAndMatchQuality matchedPatient : matches) {
            Patient patientEntry = matchedPatient.getPatient();
            SimpleObject patientSimple;
            if (patientEntry instanceof MpiPatient) {
                patientSimple = SimpleObject.fromObject(patientEntry, ui, determinePropertiesToInclude(app, MPI_PATIENT_PROPERTIES));
            } else {
                patientSimple = SimpleObject.fromObject(patientEntry, ui, determinePropertiesToInclude(app, PATIENT_PROPERTIES));
            }
            addIdentifiersToPatientSimple(patientEntry, patientSimple);
            result.add(patientSimple);
        }
        return result;
    }

    private void addIdentifiersToPatientSimple(Patient patientEntry, SimpleObject patientSimple) {
        LinkedList<SimpleObject> identifiersList = new LinkedList<SimpleObject>();
        for (PatientIdentifier identifier : patientEntry.getIdentifiers()) {
            SimpleObject identifierEntry = new SimpleObject();
            identifierEntry.put("name", identifier.getIdentifierType().getName());
            identifierEntry.put("value", identifier.getIdentifier());
            if (identifier.isPreferred()) {
                identifiersList.addFirst(identifierEntry);
            } else {
                identifiersList.add(identifierEntry);
            }
        }
        patientSimple.put("identifiers", identifiersList);
    }

    private String [] determinePropertiesToInclude(AppDescriptor app, String[] defaultProperties) {
        List<String> propertiesToIncludeList = null;
        String [] propertiesToIncludeArray;

        if (app.getConfig().get("matchingPatientsPropertiesToDisplay") != null) {
            propertiesToIncludeList = new ArrayList<String>();
            if (Arrays.asList(defaultProperties).contains("mpiPatient")) {
                propertiesToIncludeList.add("mpiPatient");
            }
            if (Arrays.asList(defaultProperties).contains("sourceLocation")) {
                propertiesToIncludeList.add("sourceLocation");
            }
            if (Arrays.asList(defaultProperties).contains("mothersName")) {
                propertiesToIncludeList.add("mothersName");
            }
            if (Arrays.asList(defaultProperties).contains("phoneNumber")) {
                propertiesToIncludeList.add("phoneNumber");
            }
            Iterator<JsonNode> i = app.getConfig().get("matchingPatientsPropertiesToDisplay").getElements();
            while (i.hasNext()) {
                propertiesToIncludeList.add(i.next().getTextValue());
            }
            addRequiredPropertiesToInclude(propertiesToIncludeList);  // these are properties hardcoded into the default template, so must be included
        }

        if (propertiesToIncludeList != null) {
            propertiesToIncludeArray = propertiesToIncludeList.toArray(new String[propertiesToIncludeList.size()]);
        }
        else {
            propertiesToIncludeArray =  defaultProperties;
        }

            return propertiesToIncludeArray;
    }

    private void addRequiredPropertiesToInclude(List<String> propertiesToInclude) {
        addIfMissing("uuid", propertiesToInclude);
        addIfMissing("patientId", propertiesToInclude);
        addIfMissing("givenName", propertiesToInclude);
        addIfMissing("familyName", propertiesToInclude);
        addIfMissing("gender", propertiesToInclude);
        addIfMissing("personAddress", propertiesToInclude);
        addIfMissing("birthdate", propertiesToInclude);
        addIfMissing("sourceLocation", propertiesToInclude);
        addIfMissing("mothersName", propertiesToInclude);
        addIfMissing("phoneNumber", propertiesToInclude);
    }

    private void addIfMissing(String property, List<String> propertiesToInclude) {
        if (!propertiesToInclude.contains(property)) {
            propertiesToInclude.add(property);
        }
    }

    private Integer determineMaxResults(AppDescriptor app) {
        if (app.getConfig().get("maxPatientSearchResults") != null) {
            return app.getConfig().get("maxPatientSearchResults").getIntValue();
        }
        else {
            return MAX_RESULTS;
        }
    }
}
