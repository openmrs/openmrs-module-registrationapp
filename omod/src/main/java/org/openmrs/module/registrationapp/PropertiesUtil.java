package org.openmrs.module.registrationapp;

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

    public static PatientIdentifierType getIdentifierTypeByGlobalProperty(String globalProperty) {
        String uuid = getGlobalProperty(globalProperty);
        PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(uuid);
        if (patientIdentifierType == null) {
            throw new APIException(String.format("Patient identifier type relevant to "
                    + "'%s' Global Property with '%s' value does not exist", globalProperty, uuid));
        }
        return patientIdentifierType;
    }

    private PropertiesUtil() {
    }
}
