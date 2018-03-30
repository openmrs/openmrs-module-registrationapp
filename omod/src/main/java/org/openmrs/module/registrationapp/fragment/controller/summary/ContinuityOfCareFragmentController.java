package org.openmrs.module.registrationapp.fragment.controller.summary;

import org.openmrs.ui.framework.fragment.FragmentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContinuityOfCareFragmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContinuityOfCareFragmentController.class);

    public void controller(FragmentModel model) {

        model.addAttribute("CCDDate", getCCDDate());
        model.addAttribute("isCCDAvailable", String.valueOf(isCCDAvailable()));
    }

    private String getCCDDate() {
        return "DD/MM/YYYY";
    }

    private boolean isCCDAvailable() {
        return true;
    }

    public void viewCCD() {
        LOGGER.info("View CCD");
    }

    public void importCCD() {
        LOGGER.info("Import CCD");
    }
}
