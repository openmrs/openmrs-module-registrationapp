/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationapp;


import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class RegistrationAppActivator extends BaseModuleActivator {

    @Override
    public void started() {
        setupIdentifierTypeGlobalProperties(Context.getAdministrationService(), Context.getService(IdentifierSourceService.class));

        super.started();
    }

    private void setupIdentifierTypeGlobalProperties(AdministrationService administrationService, IdentifierSourceService identifierSourceService) {
        // set RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID based off the autogeneration options of the primary
        // identifier type from the EMR API module
        EmrApiProperties emrApiProperties = Context.getRegisteredComponents(EmrApiProperties.class).iterator().next();
        PatientIdentifierType primaryIdentifierType = emrApiProperties.getPrimaryIdentifierType();

        IdentifierSource sourceForPrimaryType = identifierSourceService.getAutoGenerationOption(primaryIdentifierType).getSource();

        administrationService.setGlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID, sourceForPrimaryType.getId().toString());
    }

}
