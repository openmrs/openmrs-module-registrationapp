package org.openmrs.module.registrationapp.fragment.controller.summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.registrationapp.RegistrationAppConstants;
import org.openmrs.module.registrationapp.converter.RegistrationSummaryExtensionsGenerator;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

public class RegistrationSummaryFragmentController {

    public void controller(FragmentConfiguration config,
                           FragmentModel model,
                           @SpringBean AppFrameworkService appFrameworkService,
                           @SpringBean("adminService") AdministrationService administrationService,
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
    	
    	List<Extension> ret = new ArrayList<Extension>() {{ addAll(extensionList); }};
    	
    	CollectionUtils.filter(ret, new Predicate<Extension>() {
    		@Override
    		public boolean evaluate(Extension extension) {
       		 	if(extensionPointId.equals(extension.getExtensionPointId())) {
	              return true;
	            }
	            return false;
	        }
	    });
    	return ret; 
    }
}
