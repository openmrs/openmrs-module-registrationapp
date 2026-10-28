package org.openmrs.module.registrationapp.page.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.openmrs.module.appframework.domain.AppDescriptor;

public class RegisterPatientPageControllerTest {
	
	private AppDescriptor appWithBanner(String banner) {
		ObjectNode config = JsonNodeFactory.instance.objectNode();
		if (banner != null) {
			config.put("similarPatientsBanner", banner);
		}
		AppDescriptor app = new AppDescriptor();
		app.setConfig(config);
		return app;
	}
	
	@Test
	public void getSimilarPatientsBanner_shouldDefaultToExpandable() {
		assertThat(RegisterPatientPageController.getSimilarPatientsBanner(appWithBanner(null)), is("expandable"));
		assertThat(RegisterPatientPageController.getSimilarPatientsBanner(appWithBanner("somethingElse")), is("expandable"));
		assertThat(RegisterPatientPageController.getSimilarPatientsBanner(appWithBanner("expandable")), is("expandable"));
	}
	
	@Test
	public void getSimilarPatientsBanner_shouldReturnNoteAndNone() {
		assertThat(RegisterPatientPageController.getSimilarPatientsBanner(appWithBanner("note")), is("note"));
		assertThat(RegisterPatientPageController.getSimilarPatientsBanner(appWithBanner("none")), is("none"));
	}
}
