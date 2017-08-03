package org.openmrs.module.registrationapp.page.controller;


import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
public class RegistrationSummaryPageController extends AbstractRegistrationAppPageController {

    public Object controller(@RequestParam("patientId") Patient patient, PageModel model,
                             // see: https://issues.openmrs.org/browse/RA-1405 for why we support both app and appId
                             @RequestParam(value = "app", required = false) AppDescriptor app,
                             @RequestParam(value = "appId", required = false) AppDescriptor appId,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
                             @RequestParam(value = "breadcrumbOverrideProvider", required = false) String breadcrumbOverrideProvider,
                             @RequestParam(value = "breadcrumbOverridePage", required = false) String breadcrumbOverridePage,
                             @RequestParam(value = "breadcrumbOverrideLabel", required = false) String breadcrumbOverrideLabel,
                             @RequestParam(value = "breadcrumbOverrideApp", required = false) String breadcrumbOverrideAppId,
                             @InjectBeans PatientDomainWrapper patientDomainWrapper,
                             @SpringBean AppFrameworkService appFrameworkService,
                             @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
                             UiUtils ui,
                             UiSessionContext sessionContext) {

        // see: https://issues.openmrs.org/browse/RA-1405 for why we support both app and appId
        app = app != null ? app : appId;

        // TODO handle error case of patient == null
        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

        // generate the breadcrumb override if necessary--this is to support alternative entry points to this page
        if (StringUtils.isBlank(breadcrumbOverride) && StringUtils.isNotBlank(breadcrumbOverrideProvider)
                && StringUtils.isNotBlank(breadcrumbOverridePage) && StringUtils.isNotBlank(breadcrumbOverrideLabel)) {
            breadcrumbOverride = generateBreadcrumbOverride(breadcrumbOverrideLabel, breadcrumbOverrideProvider, breadcrumbOverridePage, breadcrumbOverrideAppId, ui);
        }

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("patient", patientDomainWrapper);
        model.addAttribute("appId", app !=null ? app.getId() : "referenceapplication.registrationapp.registerPatient");
        model.addAttribute("search", search);
        model.addAttribute("breadcrumbOverride", breadcrumbOverride);

        applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.includeFragments");
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);


        return null;

    }
}
