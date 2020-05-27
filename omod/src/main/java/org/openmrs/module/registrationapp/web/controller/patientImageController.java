package org.openmrs.module.registrationapp.web.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.openmrs.module.emrapi.person.image.PersonImage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.ObjectResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class patientImageController {

    protected final Log log = LogFactory.getLog(patientImageController.class);

    @RequestMapping(value = "patientimage", method = RequestMethod.POST)
    public FragmentActionResult uploadPatientImage(MultipartHttpServletRequest request) throws IOException {
        // request.getParameterMap()()
        String uuid = request.getParameter("uuid");
        String redirectUrl = request.getParameter("redirectUrl");
        log.warn("in the controller responseMessage " + redirectUrl);
        
        try {
            Iterator<String> fileNameIterator = request.getFileNames(); // Looping through the uploaded file names.

            while (fileNameIterator.hasNext()) {
                String uploadedFileName = fileNameIterator.next();
                MultipartFile multipartFile = request.getFile(uploadedFileName);
                byte[] filecontent = multipartFile.getBytes();
                log.warn("filecontent" + filecontent);
                String base64EncodedImage = Base64.getEncoder().encodeToString(filecontent);
                PersonImage personImage = new PersonImage();
                log.warn("personImage" + personImage);

                personImage.setPerson(Context.getPersonService().getPersonByUuid(uuid));
                personImage.setBase64EncodedImage(base64EncodedImage);
                EmrPersonImageService emrPersonImageService = Context.getService(EmrPersonImageService.class);
                emrPersonImageService.savePersonImage(personImage);

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new FailureResult(e.getMessage());
        }
        SimpleObject result = SimpleObject.create("redirectUrl", redirectUrl);
        ObjectResult response = new ObjectResult(result);
        return response;
    }
}