package org.openmrs.module.registrationapp.fragment.controller.summary;


import org.openmrs.Patient;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Collections;
import java.util.List;

public class RegistrationSummaryFragmentController {

    public void controller(FragmentConfiguration config,
                           FragmentModel model,
                           @SpringBean AppFrameworkService appFrameworkService,
                           @InjectBeans PatientDomainWrapper patientDomainWrapper,
                           UiSessionContext sessionContext
                           ) {

        config.require("patient");
        AppContextModel appContextModel = new AppContextModel();

        Object patient = config.get("patient");
        if (patient instanceof Patient) {
            patientDomainWrapper.setPatient((Patient) patient);
            appContextModel.put("patient", new PatientContextModel((Patient) patient));
        } else if (patient instanceof PatientDomainWrapper) {
            patientDomainWrapper = (PatientDomainWrapper) patient;
            appContextModel.put("patient", new PatientContextModel(((PatientDomainWrapper) patient).getPatient()));
        }
        model.addAttribute("patient", patientDomainWrapper);
        model.addAttribute("appContextModel", appContextModel);

        AppDescriptor app = null;
        if (config.get("appId") !=null ) {
            app = appFrameworkService.getApp((String) config.get("appId"));
        }
        model.addAttribute("appId", app !=null ? app.getId() : null);

        List<Extension> registrationFragments = appFrameworkService.getExtensionsForCurrentUser("registrationSummary.contentFragments");
        Collections.sort(registrationFragments);
        model.addAttribute("registrationFragments", registrationFragments);


        List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser("registrationSummary.overallActions");
        Collections.sort(overallActions);
        model.addAttribute("overallActions", overallActions);

    }


}
