package org.openmrs.module.registrationapp.converter;

import java.io.InputStream;
import java.util.List;
import org.junit.Test;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ExtensionConfigGeneratorTest {

	private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
	
    @Test
    public void generateShouldTranslateAppConfigToSummayConfig() throws Exception{
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream("registration_app.json");    	
    	List<AppDescriptor> appDescriptors = new ObjectMapper().readValue(inputStream, new TypeReference<List<AppDescriptor>>() {});
    	
    	List<Extension> extensions = ExtensionConfigGenerator.generate(appDescriptors.get(0));
    	
    	assertNotNull(extensions);
    	assertEquals(4, extensions.size());
    	for (Extension extn : extensions) {
	    	assertNotNull(extn.getId());
	    	assertNotNull(extn.getAppId());
	    	assertNotNull(extn.getExtensionPointId());
	    	assertNotNull(extn.getExtensionParams());
    	}
    	
    }
}
