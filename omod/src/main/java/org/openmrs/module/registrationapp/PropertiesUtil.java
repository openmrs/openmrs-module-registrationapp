package org.openmrs.module.registrationapp;

import static org.openmrs.module.registrationcore.RegistrationCoreConstants.GP_BIOMETRICS_NATIONAL_PERSON_IDENTIFIER_TYPE_UUID;
import static org.openmrs.module.registrationcore.RegistrationCoreConstants.GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

public final class PropertiesUtil {

    public static String getGlobalProperty(String propertyName) {
        String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName);
        if (StringUtils.isBlank(propertyValue)) {
            throw new APIException(String.format("Property value for '%s' is not set", propertyName));
        }
        return propertyValue;
    }

    public static boolean globalPropertySet(String propertyName) {
        String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName);
        return !StringUtils.isBlank(propertyValue);
    }

    public static boolean nationalFpTypeSet() {
        return globalPropertySet(GP_BIOMETRICS_NATIONAL_PERSON_IDENTIFIER_TYPE_UUID);
    }

    public static PatientIdentifierType getNationalFpType() {
        return getIdentifierTypeByGlobalProperty(GP_BIOMETRICS_NATIONAL_PERSON_IDENTIFIER_TYPE_UUID);
    }

    public static boolean localFpTypeSet() {
        return globalPropertySet(GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID);
    }

    public static PatientIdentifierType getLocalFpType() {
        return getIdentifierTypeByGlobalProperty(GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID);
    }

    public static PatientIdentifierType getIdentifierTypeByGlobalProperty(String globalProperty) {
        String uuid = getGlobalProperty(globalProperty);
        PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(uuid);
        if (patientIdentifierType == null) {
            throw new APIException(String.format("Patient identifier type relevant to "
                    + "'%s' Global Property with '%s' value does not exist", globalProperty, uuid));
        }
        return patientIdentifierType;
    }

    public static PatientIdentifierType getIsantePlusIdType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(
                "05a29f94-c0ed-11e2-94be-8c13b969e334");
    }
    public static PatientIdentifierType getCodeStIdType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(
                "d059f6d0-9e42-4760-8de1-8316b48bc5f1");
    }
    public static PatientIdentifierType getCodeNationalIdType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(
                "9fb4533d-4fd5-4276-875b-2ab41597f5dd");
    }
    public static PatientIdentifierType getCodePcIdType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(
                "b7a154fd-0097-4071-ac09-af11ee7e0310");
    }

    private PropertiesUtil() {
    }
}
