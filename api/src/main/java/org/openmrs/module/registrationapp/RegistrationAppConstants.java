package org.openmrs.module.registrationapp;

public class RegistrationAppConstants {

    public static final String FIND_PATIENT_FRAGMENTS_EXTENSION_POINT = "registrationapp.findpatient.fragments";
 
    /**
     * This is the name of the global property that controls whether auto-generated registration summary widgets
     * should be distributed across the left and right columns (value "true") or not (value "false").
     */
    public static final String DISTRIBUTE_SUMMARY_WIDGETS = "registrationapp.summarywidgets.distribute";
    
    /**
     * The name of the global property that controls whether to display the paper record number column on the
     * registration search results table.
     */
    public static final String GP_PAPER_RECORD_IDENTIFIER_DEFINITION = "registrationapp.paperRecordIdentifierDefinition";
}
