package org.openmrs.module.registrationapp.fragment.controller.summary;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.registrationapp.RegistrationAppConstants;
import org.openmrs.module.registrationapp.converter.RegistrationSummaryExtensionsGenerator;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegistrationSummaryFragmentController {

    public void controller(FragmentConfiguration config,
                           FragmentModel model,
                           @SpringBean AppFrameworkService appFrameworkService,
                           @SpringBean("adminService") AdministrationService administrationService,
                           @SpringBean("adtService") AdtService adtService,
                           @InjectBeans PatientDomainWrapper patientDomainWrapper,
                           @RequestParam(value = "search", required = false) String search, // context for going back to registration landing page
                           UiSessionContext sessionContext
                           ) {

        config.require("patient");
        AppContextModel appContextModel = sessionContext.generateAppContextModel();

        Object patient = config.get("patient");
        if (patient instanceof Patient) {
            patientDomainWrapper.setPatient((Patient) patient);
            appContextModel.put("patient", new PatientContextModel((Patient) patient));
        } else if (patient instanceof PatientDomainWrapper) {
            patientDomainWrapper = (PatientDomainWrapper) patient;
            appContextModel.put("patient", new PatientContextModel(((PatientDomainWrapper) patient).getPatient()));
        }

        appContextModel.put("search", search); // TODO consider putting all request params in the module in some structured way
        appContextModel.put("patientId", patientDomainWrapper != null ? patientDomainWrapper.getPatient().getUuid() : null);  // support legacy substitution methods that use "{{patientId}}" as a template and expect a uuid substitution
        appContextModel.put("visit", null);

        try {
            Location visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
            if (visitLocation != null && patientDomainWrapper != null) {
                VisitDomainWrapper activeVisit = adtService.getActiveVisit(patientDomainWrapper.getPatient(), visitLocation);
                if (activeVisit != null) {
                    appContextModel.put("visit", new VisitContextModel(activeVisit));
                }
            }
        }
        catch (IllegalArgumentException ex) {
            // location does not support visits
        }

        model.addAttribute("patient", patientDomainWrapper);
        model.addAttribute("appContextModel", appContextModel);

        AppDescriptor app = null;
        if (config.get("appId") !=null ) {
            app = appFrameworkService.getApp((String) config.get("appId"));
        }
        model.addAttribute("appId", app !=null ? app.getId() : "referenceapplication.registrationapp.registerPatient");

        List<Extension> firstColumnFragments = appFrameworkService.getExtensionsForCurrentUser("registrationSummary.contentFragments", appContextModel);
        List<Extension> secondColumnFragments = appFrameworkService.getExtensionsForCurrentUser("registrationSummary.secondColumnContentFragments", appContextModel);

        // if no summary widget(s) provided then auto-generate them from the registration app configuration
        if (CollectionUtils.isEmpty(firstColumnFragments) && CollectionUtils.isEmpty(secondColumnFragments)) {
        	String distribute = administrationService.getGlobalProperty(RegistrationAppConstants.DISTRIBUTE_SUMMARY_WIDGETS, "false");

        	List<Extension> extensions = RegistrationSummaryExtensionsGenerator.generate(app, "true".equalsIgnoreCase(distribute));

        	firstColumnFragments = filter(extensions, "registrationSummary.contentFragments");
        	secondColumnFragments = filter(extensions, "registrationSummary.secondColumnContentFragments");
        }

        Collections.sort(firstColumnFragments);
        model.addAttribute("firstColumnFragments", firstColumnFragments);

        Collections.sort(secondColumnFragments);
        model.addAttribute("secondColumnFragments", secondColumnFragments);

        List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser("registrationSummary.overallActions", appContextModel);
        Collections.sort(overallActions);
        model.addAttribute("overallActions", overallActions);

    }

    /**
     * Filters an extension list by selecting extensions that match a given extension point ID.
     */
    protected List<Extension> filter(final List<Extension> extensionList, final String extensionPointId) {

    	List<Extension> ret = new ArrayList<Extension>();
    	ret.addAll(extensionList);

    	CollectionUtils.filter(ret, new Predicate<Extension>() {
    		@Override
    		public boolean evaluate(Extension extension) {
       		 	return extensionPointId.equals(extension.getExtensionPointId());
	        }
	    });
    	return ret;
    }
}
