package org.openmrs.module.registrationapp;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import java.lang.reflect.Field;

public class RegistrationAppUtils {

    // TODO stolen from Html Form Entry module; this should ideally be a common library module

    /**
     * Get the concept by id where the id can either be:
     *   1) an integer id like 5090
     *   2) a mapping type id like "XYZ:HT"
     *   3) a uuid like "a3e12268-74bf-11df-9768-17cfc9833272"
     *   4) the fully qualified name of a Java constant that contains one of above
     *
     * @param id the concept identifier
     * @return the concept if exist, else null
     * @should find a concept by its conceptId
     * @should find a concept by its mapping
     * @should find a concept by its uuid
     * @should find a concept by static constant
     * @should return null otherwise
     * @should find a concept by its mapping with a space in between
     */
    public static Concept getConcept(String id, ConceptService conceptService) {

        Concept cpt = null;

        if (id != null) {

            id = id.trim();

            // see if this is a parseable int; if so, try looking up concept by id
            try { //handle integer: id
                int conceptId = Integer.parseInt(id);
                cpt = conceptService.getConcept(conceptId);

                if (cpt != null) {
                    return cpt;
                }
            }
            catch (Exception ex) {
                //do nothing
            }

            // handle  mapping id: xyz:ht
            int index = id.indexOf(":");
            if (index != -1) {
                String mappingCode = id.substring(0, index).trim();
                String conceptCode = id.substring(index + 1, id.length()).trim();
                cpt = conceptService.getConceptByMapping(conceptCode, mappingCode);

                if (cpt != null) {
                    return cpt;
                }
            }

            // handle uuid id: "a3e1302b-74bf-11df-9768-17cfc9833272", if the id matches a uuid format
            if (isValidUuidFormat(id)) {
                cpt = conceptService.getConceptByUuid(id);
            }
            // finally, if input contains at least one period handle recursively as a code constant
            else if (id.contains(".")) {
                return getConcept(evaluateStaticConstant(id), conceptService);
            }
        }

        return cpt;
    }


    /***
     * Determines if the passed string is in valid uuid format By OpenMRS standards, a uuid must be
     * 36 characters in length and not contain whitespace, but we do not enforce that a uuid be in
     * the "canonical" form, with alphanumerics seperated by dashes, since the MVP dictionary does
     * not use this format (We also are being slightly lenient and accepting uuids that are 37 or 38
     * characters in length, since the uuid data field is 38 characters long)
     */
    public static boolean isValidUuidFormat(String uuid) {
        if (uuid.length() < 36 || uuid.length() > 38 || uuid.contains(" ") || uuid.contains(".")) {
            return false;
        }

        return true;
    }

    /**
     * Evaluates the specified Java constant using reflection
     * @param fqn the fully qualified name of the constant
     * @return the constant value
     */
    protected static String evaluateStaticConstant(String fqn) {
        int lastPeriod = fqn.lastIndexOf(".");
        String clazzName = fqn.substring(0, lastPeriod);
        String constantName = fqn.substring(lastPeriod + 1);

        try {
            Class<?> clazz = Context.loadClass(clazzName);
            Field constantField = clazz.getField(constantName);
            Object val = constantField.get(null);
            return val != null ? String.valueOf(val) : null;
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Unable to evaluate " + fqn, ex);
        }
    }

}
