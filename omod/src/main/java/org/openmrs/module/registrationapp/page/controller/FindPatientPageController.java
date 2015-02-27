package org.openmrs.module.registrationapp.page.controller;


import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FindPatientPageController {

    public void controller(UiUtils ui,
                           UiSessionContext uiSessionContext,
                           PageModel model,
                           @RequestParam("appId") AppDescriptor app,
                           EmrApiProperties emrApiProperties,
                           @SpringBean("encounterService") EncounterService encounterService
                           ) {

        Location currentLocation = uiSessionContext.getSessionLocation();

        List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(
                (app.getConfig().get("registrationEncounter").get("encounterType").getTextValue()));

        if (encounterType == null) {
            throw new IllegalStateException("No encounter type with uuid " + app.getConfig().get("encounterTypeUuid").getTextValue());
        }

        encounterTypes.add(encounterType);

        List<Encounter> encounters = encounterService.getEncounters(null, currentLocation
                , new DateTime().minus(Days.ONE).toDate()
                , null, null
                , encounterTypes
                , null, null, null
                , false);


        SimpleObject appHomepageBreadcrumb = SimpleObject.create("label", ui.escapeJs(ui.message("mirebalais.checkin.title"))) ;
        model.addAttribute("breadcrumbOverride", ui.toJson(Arrays.asList(appHomepageBreadcrumb)));

        model.addAttribute("appEncounters", encounters);

        model.addAttribute("appId", app.getId());
    }
}
