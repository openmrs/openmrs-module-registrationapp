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
import java.util.Collections;
import java.util.Comparator;
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


        if (encounters != null && encounters.size() > 5) {
            Collections.sort(encounters, new Comparator<Encounter>() {
                @Override
                public int compare(Encounter o1, Encounter o2) {
                    Date o1Date = o1.getEncounterDatetime();
                    Date o2Date = o2.getEncounterDatetime();
                    return o2Date.compareTo(o1Date);
                }
            });
            encounters = encounters.subList(0,5);
        }

        SimpleObject appHomepageBreadcrumb = SimpleObject.create("label", ui.escapeJs(ui.message("registrationapp.header.label"))) ;
        SimpleObject patientPageBreadcrumb = SimpleObject.create("label", ui.escapeJs(ui.message("Patient.find")), "link", ui.thisUrlWithContextPath());

        model.addAttribute("breadcrumbOverride", ui.toJson(Arrays.asList(appHomepageBreadcrumb, patientPageBreadcrumb)));

        model.addAttribute("appEncounters", encounters);

        model.addAttribute("appId", app.getId());
    }
}
