package org.openmrs.module.registrationapp;


import junit.framework.Assert;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

import static org.junit.Assert.assertNotNull;

@SkipBaseSetup
public class RegistrationAppActivatorComponentTest extends BaseModuleContextSensitiveTest{

    @Test
    public void testActivator() throws Exception{
        RegistrationAppActivator activator = new RegistrationAppActivator();
        assertNotNull(activator);
    }
}
