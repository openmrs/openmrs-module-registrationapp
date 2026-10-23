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
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.search.FieldMatchResult;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.util.ConfigUtil;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
//TODO this class is to complex. Filtering by patient properties should be extracted into separate class.
public class MatchingPatientsFragmentController {

    private static final Log log = LogFactory.getLog(MatchingPatientsFragmentController.class);

    public static final int MAX_RESULTS = 10;
    public static final double CUTOFF = 2.0;

    // matches scoring at least this are flagged as strong matches; scores depend on the weights configured for the
    // search algorithm in use, so this is meant to be tuned per site
    public static final String STRONG_MATCH_CUTOFF_GP = "registrationapp.strongMatchCutoff";

    public static final double STRONG_MATCH_CUTOFF = 12.0;

    public static final String[] PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "gender", "birthdate", "personAddress"};

    public static final String[] MPI_PATIENT_PROPERTIES = new String[]{"uuid", "givenName", "familyName",
            "gender", "birthdate", "personAddress", "mpiPatient"};

    public List<SimpleObject> getSimilarPatients(UiSessionContext sessionContext,
                                                 @RequestParam("appId") AppDescriptor app,
                                                 @SpringBean("registrationCoreService") RegistrationCoreService service,
                                                 @ModelAttribute("patient") @BindParams Patient patient,
                                                 @ModelAttribute("personName") @BindParams PersonName name,
                                                 @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                                 @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                                                 @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                                                 @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                                                 HttpServletRequest request, UiUtils ui) throws Exception {
        NavigableFormStructure formStructure = buildFormStructure(sessionContext, app, appFrameworkService);
        addToPatient(patient, formStructure, name, address, request);

        Map<String, Object> otherDataPoints = createDataPoints(birthdateYears, birthdateMonths);

        List<PatientAndMatchQuality> matches = service.findFastSimilarPatients(patient, otherDataPoints, CUTOFF, determineMaxResults(app));
        return getSimpleObjects(app, ui, matches, formStructure, getCutoff(ConfigUtil.getProperty(STRONG_MATCH_CUTOFF_GP), STRONG_MATCH_CUTOFF));
    }

    public List<SimpleObject> getExactPatients(UiSessionContext sessionContext,
                                               @RequestParam("appId") AppDescriptor app,
                                               @SpringBean("registrationCoreService") RegistrationCoreService service,
                                               @ModelAttribute("patient") @BindParams Patient patient,
                                               @ModelAttribute("personName") @BindParams PersonName name,
                                               @ModelAttribute("personAddress") @BindParams PersonAddress address,
                                               @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                                               @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                                               @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                                               HttpServletRequest request, UiUtils ui) throws Exception {
        addToPatient(patient, buildFormStructure(sessionContext, app, appFrameworkService), name, address, request);

        Map<String, Object> otherDataPoints = createDataPoints(birthdateYears, birthdateMonths);

        List<PatientAndMatchQuality> matches = service.findPreciseSimilarPatients(patient, otherDataPoints, CUTOFF, determineMaxResults(app));
        return getSimpleObjects(app, ui, matches, null, null);
    }

    public List<SimpleObject> getBiometricMatches(UiSessionContext sessionContext,
                                                  @RequestParam("appId") AppDescriptor app,
                                                  @SpringBean("registrationCoreService") RegistrationCoreService service,
                                                  @SpringBean("patientService") PatientService patientService,
                                                  @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                                                  HttpServletRequest request, UiUtils ui) throws Exception {

        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app, appFrameworkService,
                sessionContext.generateAppContextModel());
        List<PatientAndMatchQuality> matches = getBiometricMatches(service, patientService, formStructure, request.getParameterMap());
        return getSimpleObjects(app, ui, matches, null, null);
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
                if (!biometricMatches.isEmpty()) {
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

    private NavigableFormStructure buildFormStructure(UiSessionContext sessionContext, AppDescriptor app,
                                                      AppFrameworkService appFrameworkService) throws IOException {
        return RegisterPatientFormBuilder.buildFormStructure(app, appFrameworkService, sessionContext.generateAppContextModel());
    }

    private void addToPatient(Patient patient, NavigableFormStructure formStructure, PersonName name, PersonAddress address,
            HttpServletRequest request) {
        patient.addName(name);
        patient.addAddress(address);
        RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());

        // Patient Identifiers
        List<Field> fields = formStructure.getFields();
        if (fields != null) {
            for (Field field : fields) {
                if (StringUtils.equals(field.getType(), "patientIdentifier")) {
                    String identifier = request.getParameter(field.getFormFieldName());
                    if (StringUtils.isNotBlank(identifier)) {
                        PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(field.getUuid());
                        patient.addIdentifier(new PatientIdentifier(identifier, identifierType, null));
                    }
                }
            }
        }

        String patientIdentifier = request.getParameter("patientIdentifier");
        if (StringUtils.isNotBlank(patientIdentifier)) {
            IdentifierSourceService iss = Context.getService(IdentifierSourceService.class);
            AdministrationService adminService = Context.getService(AdministrationService.class);
            String idSourceId = adminService.getGlobalProperty("registrationcore.identifierSourceId");
            if (StringUtils.isNotBlank(idSourceId)) {
                IdentifierSource source;
                try {
                    source = iss.getIdentifierSource(Integer.parseInt(idSourceId));
                }
                catch (Exception e) {
                    source = iss.getIdentifierSourceByUuid(idSourceId);
                }
                if (source != null) {
                    patient.addIdentifier(new PatientIdentifier(patientIdentifier, source.getIdentifierType(), null));
                }
            }
        }
    }

    /**
     * Formats a birthdate that has been entered on the registration form the same way the birthdate of an existing patient
     * is formatted in the matches, so the two can be shown side by side.
     */
    public SimpleObject formatBirthdate(@RequestParam("day") Integer day, @RequestParam("month") Integer month,
                                        @RequestParam("year") Integer year, UiUtils ui) {
        return SimpleObject.create("birthdate", formatBirthdate(day, month, year, ui));
    }

    static String formatBirthdate(int day, int month, int year, UiUtils ui) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        try {
            calendar.set(year, month - 1, day);
            return ui.format(calendar.getTime());
        }
        catch (IllegalArgumentException e) {
            // not a date, e.g. 31 February
            return "";
        }
    }

    private Map<String, Object> createDataPoints(Integer birthdateYears, Integer birthdateMonths) {
        Map<String, Object> otherDataPoints = new HashMap<String, Object>();
        otherDataPoints.put("birthdateYears", birthdateYears);
        otherDataPoints.put("birthdateMonths", birthdateMonths);
        return otherDataPoints;
    }

    /**
     * @param formStructure if given, the matches also carry the values of the person attributes that are fields on
     *            the registration form (keyed by form field name), so the UI can compare them to what was entered
     * @param strongCutoff if given, the matches are flagged as strong when they score at least this much, and carry
     *            the per-field results reported by the search algorithm (if any)
     */
    private List<SimpleObject> getSimpleObjects(AppDescriptor app, UiUtils ui, List<PatientAndMatchQuality> matches,
                                                NavigableFormStructure formStructure, Double strongCutoff) {
        List<SimpleObject> results = new ArrayList<SimpleObject>();

        for (PatientAndMatchQuality matchedPatient : matches) {
            Patient patientEntry = matchedPatient.getPatient();

            if (!alreadyInResults(patientEntry, results)) {
                SimpleObject patientSimple;
                if (patientEntry instanceof MpiPatient) {
                    patientSimple = SimpleObject.fromObject(patientEntry, ui, determinePropertiesToInclude(app, MPI_PATIENT_PROPERTIES));
                } else {
                    patientSimple = SimpleObject.fromObject(patientEntry, ui, determinePropertiesToInclude(app, PATIENT_PROPERTIES));
                }
                addIdentifiersToPatientSimple(app, patientEntry, patientSimple);
                patientSimple.put("score", matchedPatient.getScore());
                if (strongCutoff != null) {
                    patientSimple.put("strong", isStrong(matchedPatient.getScore(), strongCutoff));
                    patientSimple.put("fieldResults", toSimpleFieldResults(matchedPatient.getFieldResults()));
                }
                if (formStructure != null) {
                    patientSimple.put("attributes", getFormAttributeValues(formStructure, patientEntry));
                }
                results.add(patientSimple);
            }
        }
        return results;
    }

    // the value of a setting, as it comes from ConfigUtil, falling back to the default if it is missing or not a number
    static double getCutoff(String value, double defaultValue) {
        if (StringUtils.isNotBlank(value)) {
            try {
                return Double.parseDouble(value.trim());
            }
            catch (NumberFormatException e) {
                log.warn("Cutoff " + value + " is not a number, using " + defaultValue);
            }
        }
        return defaultValue;
    }

    static boolean isStrong(Double score, double strongCutoff) {
        return score != null && score >= strongCutoff;
    }

    static List<SimpleObject> toSimpleFieldResults(List<FieldMatchResult> fieldResults) {
        List<SimpleObject> simple = new ArrayList<SimpleObject>();
        for (FieldMatchResult result : fieldResults) {
            SimpleObject entry = new SimpleObject();
            entry.put("field", result.getField());
            entry.put("status", result.getStatus().name().toLowerCase());
            entry.put("score", result.getScore());
            simple.add(entry);
        }
        return simple;
    }

    static Map<String, String> getFormAttributeValues(NavigableFormStructure formStructure, Patient patient) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        List<Field> fields = formStructure.getFields();
        if (fields != null) {
            for (Field field : fields) {
                if (StringUtils.equals(field.getType(), "personAttribute")) {
                    PersonAttribute attribute = null;
                    for (PersonAttribute candidate : patient.getActiveAttributes()) {
                        if (candidate.getAttributeType().getUuid().equals(field.getUuid())) {
                            attribute = candidate;
                            break;
                        }
                    }
                    if (attribute != null && StringUtils.isNotBlank(attribute.getValue())) {
                        values.put(field.getFormFieldName(), attribute.getValue());
                    }
                }
            }
        }
        return values;
    }

    private Boolean alreadyInResults(Patient patient, List<SimpleObject> results) {
        for (SimpleObject result : results) {
            if (result.get("uuid").toString().equals(patient.getUuid())) {
                return true;
            }
        }
        return false;
    }

    private void addIdentifiersToPatientSimple(AppDescriptor app, Patient patientEntry, SimpleObject patientSimple) {

        LinkedList<SimpleObject> identifiersList = new LinkedList<SimpleObject>();
        List<String> identifierTypesToInclude = null;

        if (app.getConfig().get("identifierTypesToDisplay") != null) {
            identifierTypesToInclude = new ArrayList<String>();
            Iterator<JsonNode> i = app.getConfig().get("identifierTypesToDisplay").getElements();
            while (i.hasNext()) {
                identifierTypesToInclude.add(i.next().getTextValue());
            }
        }

        for (PatientIdentifier identifier : patientEntry.getActiveIdentifiers()) {

            if (shouldIncludeIdentifier(identifier, identifierTypesToInclude)) {
                SimpleObject identifierEntry = new SimpleObject();
                identifierEntry.put("name", identifier.getIdentifierType().getName());
                identifierEntry.put("value", identifier.getIdentifier());
                if (identifier.isPreferred()) {
                    identifiersList.addFirst(identifierEntry);
                } else {
                    identifiersList.add(identifierEntry);
                }
            }
        }
        patientSimple.put("identifiers", identifiersList);
    }

    private boolean shouldIncludeIdentifier(PatientIdentifier identifier, List<String> identifierTypesToInclude) {
        // if the parameter has not been specified, show all
        if (identifierTypesToInclude == null) {
            return true;
        }

        for (String identifierType : identifierTypesToInclude) {
            if (identifier.getIdentifierType().getUuid().equals(identifierType) ||
                    identifier.getIdentifierType().getName().equals(identifierType)) {
                return true;
            }
        }
        return false;
    }

    private String [] determinePropertiesToInclude(AppDescriptor app, String[] defaultProperties) {
        List<String> propertiesToIncludeList = null;
        String [] propertiesToIncludeArray;

        if (app.getConfig().get("matchingPatientsPropertiesToDisplay") != null) {
            propertiesToIncludeList = new ArrayList<String>();
            if (Arrays.asList(defaultProperties).contains("mpiPatient")) {
                propertiesToIncludeList.add("mpiPatient");
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
