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
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


        List<Encounter> registrationEncounters = new ArrayList<Encounter>();

        if (encounters != null && encounters.size() > 0) {
            Collections.sort(encounters, new Comparator<Encounter>() {
                @Override
                public int compare(Encounter o1, Encounter o2) {
                    Date o1Date = o1.getDateCreated();
                    Date o2Date = o2.getDateCreated();
                    return o2Date.compareTo(o1Date);
                }
            });

            //display last 5 registration encounters only for distinct patients
            Set<Integer> patientIds = new HashSet<Integer>();
            for (Encounter encounter : encounters) {
                Integer patientId = encounter.getPatient().getId();
                if (!encounter.getPatient().isVoided() && !patientIds.contains(patientId)){
                    patientIds.add(patientId);
                    if (registrationEncounters.size() < 5 ) {
                        registrationEncounters.add(encounter);
                    } else {
                        break;
                    }
                }
            }

        }

        model.addAttribute("appEncounters", registrationEncounters);
        model.addAttribute("appId", app.getId());
    }
}
