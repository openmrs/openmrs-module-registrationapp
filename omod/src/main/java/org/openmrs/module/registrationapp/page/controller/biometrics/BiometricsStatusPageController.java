package org.openmrs.module.registrationapp.page.controller.biometrics;

import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class BiometricsStatusPageController {

    public void get(@RequestParam("app")AppDescriptor app, PageModel model) throws Exception {
        model.put("config", app.getConfig());
    }

}
