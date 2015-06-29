package org.openmrs.module.registrationapp.page.controller;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

import java.util.HashMap;
import java.util.Map;

public class AbstractRegistrationAppPageController {

    protected String generateBreadcrumbOverride(String breadcrumbOverrideLabel, String breadcrumbOverrideProvider,
                                               String breadcrumbOverridePage, String appId, UiUtils ui) {
        Map<String, Object> attrs = new HashMap<String,Object>();
        if (StringUtils.isNotBlank(appId)) {
            attrs.put("appId", appId);
        }
        SimpleObject breadcrumbOverride = SimpleObject.create("label", ui.message(breadcrumbOverrideLabel), "link",
                ui.pageLink(breadcrumbOverrideProvider, breadcrumbOverridePage, attrs));
        return ui.toJson(breadcrumbOverride);
    }

}
