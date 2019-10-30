package org.openmrs.module.registrationapp.fragment.controller.summary;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.registrationapp.NameSupportCompatibility;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class SectionFragmentController {

    public void controller(FragmentConfiguration config,
                           FragmentModel model,
                           @SpringBean AppFrameworkService appFrameworkService,
                           @InjectBeans PatientDomainWrapper patientWrapper) throws Exception {

        config.require("patient");
        config.require("sectionId");
        config.require("appId");

        AppDescriptor app = appFrameworkService.getApp((String) config.get("appId"));                
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app, false);

        Object patient = config.get("patient");
        String sectionId = (String) config.get("sectionId");


        if (patient instanceof Patient) {
            patientWrapper.setPatient((Patient) patient);
            config.addAttribute("patient", patientWrapper);
        } else if (patient instanceof PatientDomainWrapper) {
            patientWrapper = (PatientDomainWrapper) patient;
        }
        
        NameSupportCompatibility nameSupport = Context.getRegisteredComponent(NameSupportCompatibility.ID, NameSupportCompatibility.class);

        model.put("nameTemplate", nameSupport.getDefaultLayoutTemplate());
        model.put("section", formStructure.getSections().get(sectionId));
        model.put("uiUtils", new RegistrationAppUiUtils());
    }

}
