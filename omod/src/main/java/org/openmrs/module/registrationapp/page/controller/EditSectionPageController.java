
package org.openmrs.module.registrationapp.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.EventMessage;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.registrationapp.AddressSupportCompatibility;
import org.openmrs.module.registrationapp.NameSupportCompatibility;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationapp.model.Section;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.RegistrationCoreUtil;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricData;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.uicommons.UiCommonsConstants;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.openmrs.validator.PatientValidator;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditSectionPageController {

    protected final Log log = LogFactory.getLog(EditSectionPageController.class);

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("patientId") Patient patient,
                    @RequestParam("appId") AppDescriptor app,
                    @RequestParam(value = "returnUrl", required = false) String returnUrl,
                    @RequestParam("sectionId") String sectionId,
                    @SpringBean("adminService") AdministrationService administrationService) throws Exception {

        sessionContext.requireAuthentication();

        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app, false);
        addModelAttributes(model, patient, formStructure.getSections().get(sectionId), administrationService, returnUrl,
                app);
    }

    /**
     * @should void the old person address and replace it with a new one when it is edited
     * @should void the old person address and replace it with a new one when it is edited
     * @should not void the existing address if there are no changes
     */
    public String post(UiSessionContext sessionContext, PageModel model,
                       @RequestParam("patientId") @BindParams Patient patient,
                       @BindParams PersonAddress address,
                       @BindParams PersonName name,
                       @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                       @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                       @RequestParam("appId") AppDescriptor app,
                       @RequestParam("sectionId") String sectionId,
                       @RequestParam("returnUrl") String returnUrl,
                       @SpringBean("patientService") PatientService patientService,
                       @SpringBean("personService") PersonService personService,
                       @SpringBean("registrationCoreService") RegistrationCoreService registrationCoreService,
                       @SpringBean("adminService") AdministrationService administrationService, HttpServletRequest request,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService, Session session,
                       @SpringBean("patientValidator") PatientValidator patientValidator, UiUtils ui) throws Exception {

        sessionContext.requireAuthentication();

        // handle person name, if present
        if (patient.getPersonName() != null && name != null && StringUtils.isNotBlank(name.getFullName())) {  // bit of a hack because it seems that in this case name is never null, so we
            PersonName currentName = patient.getPersonName();
            if (!currentName.equalsContent(name)) {
                //void the old name and replace it with the new one
                patient.addName(name);
                currentName.setVoided(true);
            }
        }

        // handle birthdate estimate, if no birthdate but estimate present
        if (patient.getBirthdate() == null && (birthdateYears != null || birthdateMonths != null)) {
            patient.setBirthdateEstimated(true);
            patient.setBirthdate(RegistrationCoreUtil.calculateBirthdateFromAge(birthdateYears, birthdateMonths, null, null));
        }

        // handle person address, if present
        if (address != null && !address.isBlank()) {
            PersonAddress currentAddress = patient.getPersonAddress();
            if (currentAddress != null) {
                if (!currentAddress.equalsContent(address)) {
                    //void the old address and replace it with the new one
                    patient.addAddress(address);
                    currentAddress.setVoided(true);
                }
            }
            else {
                patient.addAddress(address);
            }
        }

        // handle patient relationships if present
        if (request.getParameterMap().containsKey("relationship_type") && request.getParameterMap().containsKey("other_person_uuid")){
        	updatePatientRelationships(patient, request.getParameterValues("relationship_type"), request.getParameterValues("other_person_uuid"), personService);
        }
        
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app, false);

        BindingResult errors = new BeanPropertyBindingResult(patient, "patient");
        patientValidator.validate(patient, errors);
        RegistrationAppUiUtils.validateLatitudeAndLongitudeIfNecessary(address, errors);

        if (formStructure != null) {
            RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());

            try {
                RegisterPatientFormBuilder.resolvePatientIdentifierFields(formStructure, patient, request.getParameterMap());
            }
            catch (Exception ex) {
                RegistrationAppUiUtils.checkForIdentifierExceptions(ex, errors);
            }

            // handle any biometrics data that has been added
            Map<Field, BiometricSubject> fingerprintData = RegisterPatientFormBuilder.extractBiometricDataFields(formStructure, request.getParameterMap());
            for (Field fingerprintField : fingerprintData.keySet()) {
                BiometricSubject subject = fingerprintData.get(fingerprintField);
                PatientIdentifierType identifierType = patientService.getPatientIdentifierTypeByUuid(fingerprintField.getUuid());
                if (identifierType == null) {
                    throw new IllegalStateException("Invalid fingerprint configuration. No patient identifier type with uuid [" + fingerprintField.getUuid() + "] found.");
                }
                registrationCoreService.saveBiometricsForPatient(patient, new BiometricData(subject, identifierType));
            }
        }

        if (!errors.hasErrors()) {
            try {
                //The person address changes get saved along as with the call to save patient
                patientService.savePatient(patient);
                if (sectionId.equals("contactInfo")) {
                    InfoErrorMessageUtil.flashInfoMessage(request.getSession(),
                            ui.message("registrationapp.editContactInfoMessage.success", patient.getPersonName() != null ? ui.encodeHtml(patient.getPersonName().toString()) : ""));
                }
                else {
                    String sectionLabel= formStructure.getSections().get(sectionId).getLabel();
                    InfoErrorMessageUtil.flashInfoMessage(request.getSession(),
                            ui.message("registrationapp.editCustomSectionInfoMessage.success", patient.getPersonName() != null ? ui.encodeHtml(patient.getPersonName().toString()) : "", sectionLabel));
                }
                EventMessage eventMessage = new EventMessage();
                eventMessage.put(RegistrationCoreConstants.KEY_PATIENT_UUID, patient.getUuid());
                Event.fireEvent(RegistrationCoreConstants.PATIENT_EDIT_EVENT_TOPIC_NAME, eventMessage);

                return "redirect:" + returnUrl;
            }
            catch (Exception e) {
                log.warn("Error occurred while saving patient's contact info", e);
                session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, "registrationapp.save.fail");
            }

        } else {
            model.addAttribute("errors", errors);
            StringBuffer errorMessage = new StringBuffer(messageSourceService.getMessage("error.failed.validation"));
            errorMessage.append("<ul>");
            for (ObjectError error : errors.getAllErrors()) {
                errorMessage.append("<li>");
                errorMessage.append(messageSourceService.getMessage(error.getCode(), error.getArguments(),
                        error.getDefaultMessage(), null));
                errorMessage.append("</li>");
            }
            errorMessage.append("</ul>");
            session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, errorMessage.toString());
        }

        addModelAttributes(model, patient, formStructure.getSections().get(sectionId), administrationService, returnUrl,
                app);
        //redisplay the form
        return null;
    }


    private void addModelAttributes(PageModel model, Patient patient, Section section,
                                    AdministrationService adminService, String returnUrl,
                                    AppDescriptor app) throws Exception {

        NameSupportCompatibility nameSupport = Context.getRegisteredComponent(NameSupportCompatibility.ID, NameSupportCompatibility.class);
        AddressSupportCompatibility addressSupport = Context.getRegisteredComponent(AddressSupportCompatibility.ID, AddressSupportCompatibility.class);

        model.addAttribute("app", app);
        model.addAttribute("returnUrl", returnUrl);
        model.put("uiUtils", new RegistrationAppUiUtils());
        model.addAttribute("patient", patient);
        model.addAttribute("patientUuid", patient.getUuid());
        model.addAttribute("addressTemplate", addressSupport.getAddressTemplate());
        model.addAttribute("nameTemplate", nameSupport.getDefaultLayoutTemplate());
        model.addAttribute("section", section);
        model.addAttribute("enableOverrideOfAddressPortlet",
                adminService.getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
        model.addAttribute("relationshipTypes", Context.getPersonService().getAllRelationshipTypes());
        model.addAttribute("genderOptions", RegistrationAppUiUtils.getGenderOptions(app));
    }
    
    private void updatePatientRelationships(Patient patient, String[] types, String[] persons, PersonService personService) {
        List<Relationship> relationships = new ArrayList<Relationship>();

        for (int i = 0; i < types.length; i++) {
            if (types[i] != null && types[i].length() > 0) {
                // Remove flag characters at the end (used for relationship direction)
                String relationshipTypeUUID = types[i].substring(0, types[i].length() - 2);
                
                // Last character reveals relationship direction (aIsToB or bIsToA)
                char relationshipDirection = types[i].charAt(types[i].length() - 1);                
                if (relationshipDirection != 'A') {
                	if (relationshipDirection != 'B') {
                		throw new APIException("Relationship direction not specified");
                	}
                }
                RelationshipType rt = personService.getRelationshipTypeByUuid(relationshipTypeUUID);
                
                if (rt != null) {
                    Person otherPerson = personService.getPersonByUuid(persons[i]);
                    
                    Person personA = relationshipDirection == 'A' ? otherPerson : patient;
                    Person personB = relationshipDirection == 'B' ? otherPerson : patient;
                    if (personA != null && personB != null) {
                        relationships = personService.getRelationships(personA, personB, rt);
                        if (CollectionUtils.isEmpty(relationships)) {
                        	personService.saveRelationship(new Relationship(personA, personB, rt));
                        } else {
                        	Relationship relationship = relationships.get(0);
                        	relationship.setPersonA(personA);
                        	relationship.setPersonB(personB);
                        	relationship.setRelationshipType(rt);
                        	personService.saveRelationship(relationship);
                        }
                    }
                }
            }
        }
    }
}
