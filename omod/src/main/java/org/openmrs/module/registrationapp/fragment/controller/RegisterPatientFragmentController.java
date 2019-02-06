package org.openmrs.module.registrationapp.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.joda.time.DateTimeComparator;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.registrationapp.RegistrationAppUtils;
import org.openmrs.module.registrationapp.action.AfterPatientCreatedAction;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.RegistrationCoreUtil;
import org.openmrs.module.registrationcore.RegistrationData;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricData;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.validator.PatientValidator;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegisterPatientFragmentController {

    private final Log log = LogFactory.getLog(RegisterPatientFragmentController.class);

    class ObsGroupItem {
        String obsConcept = null;
        String[] obsValues = null;

        ObsGroupItem(){}

        ObsGroupItem(String obsConcept, String[] obsValues) {
            this.obsConcept = obsConcept;
            this.obsValues = obsValues;
        }

        public String getObsConcept() {
            return obsConcept;
        }

        public void setObsConcept(String obsConcept) {
            this.obsConcept = obsConcept;
        }

        public String[] getObsValues() {
            return obsValues;
        }

        public void setObsValue(String[] obsValues) {
            this.obsValues = obsValues;
        }
    }

    public FragmentActionResult importMpiPatient(@RequestParam("mpiPersonId") String personId,
                            @SpringBean("registrationCoreService") RegistrationCoreService registrationService) {
        String patientUuid = registrationService.importMpiPatient(personId);
        return new SuccessResult(patientUuid);
    }

    public FragmentActionResult submit(UiSessionContext sessionContext, @RequestParam(value="appId") AppDescriptor app,
                            @SpringBean("registrationCoreService") RegistrationCoreService registrationService,
                            @ModelAttribute("patient") @BindParams Patient patient,
                            @ModelAttribute("personName") @BindParams PersonName name,
                            @ModelAttribute("personAddress") @BindParams PersonAddress address,
                            @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                            @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                            @RequestParam(value="registrationDate", required = false) Date registrationDate,
                            @RequestParam(value="unknown", required = false) Boolean unknown,
                            @RequestParam(value="patientIdentifier", required = false) String patientIdentifier,
                            HttpServletRequest request,
                            @SpringBean("messageSourceService") MessageSourceService messageSourceService,
                            @SpringBean("encounterService") EncounterService encounterService,
                            @SpringBean("obsService") ObsService obsService,
                            @SpringBean("conceptService") ConceptService conceptService,
                            @SpringBean("patientService") PatientService patientService,
                            @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                            @SpringBean("patientValidator") PatientValidator patientValidator, UiUtils ui) throws Exception {


        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        if (unknown != null && unknown) {
            // TODO make "UNKNOWN" be configurable
            name.setFamilyName("UNKNOWN");
            name.setGivenName("UNKNOWN");
            patient.addAttribute(new PersonAttribute(emrApiProperties.getUnknownPatientPersonAttributeType(), "true"));
        }

        patient.addName(name);
        patient.addAddress(address);

        // handle birthdate estimate, if no birthdate but estimate present
        if (patient.getBirthdate() == null && (birthdateYears != null || birthdateMonths != null)) {
            patient.setBirthdateEstimated(true);
            patient.setBirthdate(RegistrationCoreUtil.calculateBirthdateFromAge(birthdateYears, birthdateMonths, null, null));
        }

        BindingResult errors = new BeanPropertyBindingResult(patient, "patient");
        if(formStructure!=null){
            RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());

            try {
                RegisterPatientFormBuilder.resolvePatientIdentifierFields(formStructure, patient, request.getParameterMap());
            }
            catch (Exception ex) {
                RegistrationAppUiUtils.checkForIdentifierExceptions(ex, errors);
            }
        }

        //TODO This validation code really belongs to the PersonAddressValidator in core
        RegistrationAppUiUtils.validateLatitudeAndLongitudeIfNecessary(address, errors);

        if (errors.hasErrors()) {
            return new FailureResult(createErrorMessage(errors, messageSourceService));
        }

        List<Relationship> relationships = null;

        if (request.getParameterMap().containsKey("relationship_type") && request.getParameterMap().containsKey("other_person_uuid")){
            relationships = getPatientRelationships(request.getParameterValues("relationship_type"), request.getParameterValues("other_person_uuid"));
        }

        RegistrationData registrationData = new RegistrationData();
        registrationData.setPatient(patient);
        registrationData.setRelationships(relationships);
        registrationData.setIdentifier(patientIdentifier);
        registrationData.setIdentifierLocation(sessionContext.getSessionLocation());

        // Add any biometric data that was submitted
        Map<Field, BiometricSubject> fingerprintData = RegisterPatientFormBuilder.extractBiometricDataFields(formStructure, request.getParameterMap());
        for (Field fingerprintField : fingerprintData.keySet()) {
            BiometricSubject subject = fingerprintData.get(fingerprintField);
            PatientIdentifierType identifierType = patientService.getPatientIdentifierTypeByUuid(fingerprintField.getUuid());
            if (identifierType == null) {
                throw new IllegalStateException("Invalid fingerprint configuration. No patient identifier type with uuid [" + fingerprintField.getUuid() + "] found.");
            }
            registrationData.addBiometricData(new BiometricData(subject, identifierType));
        }

        try {
            // if patientIdentifier is blank, the underlying registerPatient method should automatically generate one
            patient = registrationService.registerPatient(registrationData);
        }
        catch (Exception ex) {

            // TODO I remember getting into trouble if i called this validator before the above save method.
            // TODO Am therefore putting this here for: https://tickets.openmrs.org/browse/RA-232
            patientValidator.validate(patient, errors);
            int originalErrorCount = errors.getErrorCount();
            RegistrationAppUiUtils.checkForIdentifierExceptions(ex, errors);  // TODO do I need to check this again here since we are now calling it earlier? can keep it just to be save

            if (!errors.hasErrors() || (originalErrorCount == errors.getErrorCount())) {
                errors.reject(ex.getMessage());
            }
            return new FailureResult(createErrorMessage(errors, messageSourceService));
        }

        // now create the registration encounter, if configured to do so
        Encounter registrationEncounter = buildRegistrationEncounter(patient, registrationDate, sessionContext, app, encounterService);
        if (registrationEncounter != null) {
            encounterService.saveEncounter(registrationEncounter);
        }

        Map<String, List<ObsGroupItem>> obsGroupMap = new LinkedHashMap<String, List<ObsGroupItem>>();
        // build any obs that are submitted
        List<Obs> obsToCreate = new ArrayList<Obs>();
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
            String param = e.nextElement();
            if (param.startsWith("obsgroup.")) {
                parseObsGroup(obsGroupMap, param, request.getParameterValues(param));

            } else if (param.startsWith("obs.")) {
                String conceptUuid = param.substring("obs.".length());
                buildObs(conceptService, obsToCreate, conceptUuid, request.getParameterValues(param));
            }
        }

        if (obsGroupMap.size() > 0 ){
            buildGroupObs(conceptService, obsToCreate, obsGroupMap);
        }
        if (obsToCreate.size() > 0) {
            if (registrationEncounter != null) {
                for (Obs obs : obsToCreate) {
                    registrationEncounter.addObs(obs);
                }
                encounterService.saveEncounter(registrationEncounter);
            }
            else {
                Date datetime = registrationDate != null ? registrationDate : new Date();
                for (Obs obs : obsToCreate) {
                    // since we don't inherit anything from the Encounter, we need to specify these
                    obs.setPerson(patient);
                    obs.setLocation(sessionContext.getSessionLocation());
                    obs.setObsDatetime(datetime);
                    obsService.saveObs(obs, null);
                }
            }
        }

        // run any AfterPatientCreated actions
        // TODO wrap everything here in a single transaction
        ArrayNode afterCreatedArray = (ArrayNode) app.getConfig().get("afterCreatedActions");
        if (afterCreatedArray != null) {
            for (JsonNode actionNode : afterCreatedArray) {
                String actionString = actionNode.getTextValue();
                AfterPatientCreatedAction action;
                if (actionString.startsWith("bean:")) {
                    String beanId = actionString.substring("bean:".length());
                    action = Context.getRegisteredComponent(beanId, AfterPatientCreatedAction.class);
                } else if (actionString.startsWith("class:")) {
                    String className = actionString.substring("class:".length());
                    Class<? extends AfterPatientCreatedAction> clazz = (Class<? extends AfterPatientCreatedAction>) Context.loadClass(className);
                    action = clazz.newInstance();
                } else {
                    throw new IllegalStateException("Invalid afterCreatedAction: " + actionString);
                }

                action.afterPatientCreated(patient, request.getParameterMap());
            }
        }

        InfoErrorMessageUtil.flashInfoMessage(request.getSession(), ui.message("registrationapp.createdPatientMessage", ui.encodeHtml(ui.format(patient))));

        String redirectUrl = app.getConfig().get("afterCreatedUrl").getTextValue();
        redirectUrl = redirectUrl.replaceAll("\\{\\{patientId\\}\\}", patient.getUuid().toString());
        if (registrationEncounter != null) {
            redirectUrl = redirectUrl.replaceAll("\\{\\{encounterId\\}\\}", registrationEncounter.getId().toString());
        }

        return new SuccessResult(redirectUrl);
    }

    private void parseObsGroup(Map<String, List<ObsGroupItem>> obsGroupMap, String param, String[] parameterValues) {
        int obsIndex = param.indexOf(".obs.");
        if (obsIndex > 0 ) {
            String conceptObsGroup = param.substring("obsgroup.".length(), obsIndex);
            if (StringUtils.isNotBlank(conceptObsGroup)) {
                String conceptUuid = param.substring(obsIndex + ".obs.".length());
                if (StringUtils.isNotBlank(conceptUuid)) {
                    ObsGroupItem obsGroupItem = new ObsGroupItem(conceptUuid, parameterValues);
                    List<ObsGroupItem> obsGroupItems = obsGroupMap.get(conceptObsGroup);
                    if (obsGroupItems == null) {
                        obsGroupItems = new ArrayList<ObsGroupItem>();
                    }
                    obsGroupItems.add(obsGroupItem);
                    obsGroupMap.put(conceptObsGroup, obsGroupItems);
                }
            }
        }
    }

    private void buildGroupObs(ConceptService conceptService, List<Obs> obsToCreate, Map<String, List<ObsGroupItem>> obsGroupMap) throws ParseException {
        if (obsGroupMap != null && obsGroupMap.size() > 0 ) {
            for (String groupConceptUuid : obsGroupMap.keySet()) {
                Concept groupConcept = RegistrationAppUtils.getConcept(groupConceptUuid, conceptService);
                if (groupConcept == null) {
                    throw new IllegalArgumentException("Cannot find concept: " + groupConceptUuid);
                }
                Obs groupObs = new Obs();
                groupObs.setConcept(groupConcept);
                List<Obs> groupObsToCreate = new ArrayList<Obs>();
                List<ObsGroupItem> obsGroupItems = obsGroupMap.get(groupConceptUuid);
                for (ObsGroupItem obsGroupItem : obsGroupItems) {
                    buildObs(conceptService, groupObsToCreate, obsGroupItem.getObsConcept(), obsGroupItem.getObsValues());
                }
                if (groupObsToCreate.size() > 0) {
                    for (Obs obs : groupObsToCreate) {
                        groupObs.addGroupMember(obs);
                    }
                    obsToCreate.add(groupObs);
                }
            }
        }
    }

    private void buildObs(ConceptService conceptService, List<Obs> obsToCreate, String conceptId, String[] parameterValues) throws ParseException {
        Concept concept = RegistrationAppUtils.getConcept(conceptId, conceptService);
        if (concept == null) {
            throw new IllegalArgumentException("Cannot find concept: " + conceptId);
        }
        for (String parameterValue : parameterValues) {
            if (StringUtils.isNotBlank(parameterValue)) {
                Obs obs = new Obs();
                obs.setConcept(concept);
                if (concept.getDatatype().isCoded()) {
                    Concept valueCoded = RegistrationAppUtils.getConcept(parameterValue, conceptService);
                    if (valueCoded == null) {
                        log.error("Submitted a coded obs whose value we can't interpret: " + parameterValue);
                    }
                    obs.setValueCoded(valueCoded);
                }
                else {
                    obs.setValueAsString(parameterValue);
                }
                obsToCreate.add(obs);
            }
        }
    }


    private String createErrorMessage(BindingResult errors, MessageSourceService messageSourceService) throws Exception {
        StringBuffer errorMessage = new StringBuffer(messageSourceService.getMessage("error.failed.validation") + ":");
        errorMessage.append("<ul>");
        for (ObjectError error : errors.getAllErrors()) {
            if (!isPreferredIdentifierErrorMessage(error) || errors.getErrorCount() == 1) {
                errorMessage.append("<li>");
                errorMessage.append(messageSourceService.getMessage(error.getCode(), error.getArguments(),
                        error.getDefaultMessage(), null));
                errorMessage.append("</li>");
            }
        }
        errorMessage.append("</ul>");
        return errorMessage.toString();
    }

    private Encounter buildRegistrationEncounter(Patient patient, Date registrationDate, UiSessionContext sessionContext, AppDescriptor app, EncounterService encounterService) {

        EncounterType registrationEncounterType = getRegistrationEncounterType(app, encounterService);
        EncounterRole registrationEncounterRole = getRegistrationEncounterRole(app, encounterService);

        // if no registration encounter type specified, we aren't configured to create an encounter
        if (registrationEncounterType == null) {
            return null;
        }

        // if date not specified, or date = current date, consider this a "real-time" entry, and set date to current time
        if (DateTimeComparator.getDateOnlyInstance().compare(registrationDate, new Date()) == 0){
            registrationDate = new Date();
        }

        // create the encounter
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setEncounterType(registrationEncounterType);
        encounter.setLocation(sessionContext.getSessionLocation());
        encounter.setEncounterDatetime(registrationDate);

        if (registrationEncounterRole != null) {
            encounter.addProvider(registrationEncounterRole, sessionContext.getCurrentProvider());
        }

        return encounter;
    }


    private EncounterType getRegistrationEncounterType(AppDescriptor app, EncounterService encounterService) {

        EncounterType registrationEncounterType = null;

        if (!app.getConfig().path("registrationEncounter").path("encounterType").isMissingNode()) {
            String encounterTypeUuid =  app.getConfig().get("registrationEncounter").get("encounterType").getTextValue();
            registrationEncounterType = encounterService.getEncounterTypeByUuid(encounterTypeUuid);

            if (registrationEncounterType == null) {
                throw new IllegalArgumentException("Cannot find encounter type referenced by " + encounterTypeUuid);
            }
        }

        return registrationEncounterType;
    }

    private EncounterRole getRegistrationEncounterRole(AppDescriptor app, EncounterService encounterService) {

        EncounterRole registrationEncounterRole = null;

        if (!app.getConfig().path("registrationEncounter").path("encounterRole").isMissingNode()) {
            String encounterRoleUuid = app.getConfig().get("registrationEncounter").get("encounterRole").getTextValue();
            registrationEncounterRole = encounterService.getEncounterRoleByUuid(encounterRoleUuid);

            if (registrationEncounterRole == null) {
                throw new IllegalArgumentException("Cannot find encounter type referenced by " + encounterRoleUuid);
            }
        }

        return registrationEncounterRole;
    }

    private boolean isPreferredIdentifierErrorMessage(ObjectError error) {
        for (String code : error.getCodes()) {
            if (code.equals("error.preferredIdentifier")) {
                return true;
            }
        }
        return false;
    }

    private List<Relationship> getPatientRelationships(String[] types, String[] persons) {
        List<Relationship> relationships = new ArrayList<Relationship>();
        PersonService personService = Context.getPersonService();

        for (int i = 0; i < types.length; i++) {
            if (types[i] != null && types[i].length() > 0) {
                // Remove flag characters at the end (used for relationship direction)
                String relationshipTypeUUID = types[i].substring(0, types[i].length() - 2);
                // Last character reveals relationship direction (aIsToB or bIsToA)
                char relationshipDirection = types[i].charAt(types[i].length() - 1);

                RelationshipType rt = personService.getRelationshipTypeByUuid(relationshipTypeUUID);
                if (rt != null) {
                    Person p = personService.getPersonByUuid(persons[i]);
                    if (p != null) {
                        if (relationshipDirection == 'A') {
                            relationships.add(new Relationship(p, null, rt));
                        } else if (relationshipDirection == 'B') {
                            relationships.add(new Relationship(null, p, rt));
                        } else {
                            throw new APIException("Relationship direction not specified");
                        }
                    }
                }
            }
        }

        return relationships;
    }
}
