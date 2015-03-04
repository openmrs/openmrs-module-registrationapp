package org.openmrs.module.registrationapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


public class RegistrationAppUtilsComponentTest extends BaseModuleContextSensitiveTest {

    protected final Log log = LogFactory.getLog(getClass());

    // Most of this was taken from Html Form Entry Util

    // For testing concept lookups by static constant
    public static final int TEST_CONCEPT_CONSTANT_ID = 3;
    public static final String TEST_CONCEPT_CONSTANT_UUID = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
    public static final String TEST_CONCEPT_CONSTANT_MAPPING = "Some Standardized Terminology:WGT234";

    @Autowired
    private ConceptService conceptService;

    @Test
    @Verifies(value = "should find a concept by its conceptId", method = "getConcept(String)")
    public void getConcept_shouldFindAConceptByItsConceptId() throws Exception {
        String id = "3";
        Assert.assertEquals("3", RegistrationAppUtils.getConcept(id, conceptService).getConceptId().toString());
    }

    @Test
    @Verifies(value = "should find a concept by its mapping", method = "getConcept(String)")
    public void getConcept_shouldFindAConceptByItsMapping() throws Exception {
        String id = "Some Standardized Terminology:WGT234";
        Concept cpt = RegistrationAppUtils.getConcept(id, conceptService);
        Assert.assertEquals(5089, cpt.getId().intValue());
    }

    @Test
    @Verifies(value = "should find a concept by its uuid", method = "getConcept(String)")
    public void getConcept_shouldFindAConceptByItsUuid() throws Exception {
        //the uuid from standardTestDataset
        String id = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
        Assert.assertEquals(id, RegistrationAppUtils.getConcept(id, conceptService).getUuid());
    }

    @Test
    @Verifies(value = "should find a concept by its uuid", method = "getConcept(String)")
    public void getConcept_shouldFindAConceptWithNonStandardUuid() throws Exception {
        // concept from HtmlFormEntryTest-data.xml
        String id = "1000AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        Concept concept = conceptService.getConcept(10);
        concept.setUuid(id);
        conceptService.saveConcept(concept);
        Context.flushSession();
        Assert.assertEquals(id, RegistrationAppUtils.getConcept(id, conceptService).getUuid());
    }

    @Test
    @Verifies(value = "should not find a concept with invalid uuid", method = "getConcept(String)")
    public void getConcept_shouldNotFindAConceptWithInvalidUuid() throws Exception {
        // concept from HtmlFormEntryTest-data.xml
        String id = "1000";
        Assert.assertNull(RegistrationAppUtils.getConcept(id, conceptService));
    }

    @Test
    @Verifies(value = "should find a concept by static constant", method = "getConcept(String)")
    public void getConcept_shouldFindAConceptByStaticConstant() throws Exception {
        assertThat(RegistrationAppUtils.getConcept("org.openmrs.module.registrationapp.RegistrationAppUtilsComponentTest.TEST_CONCEPT_CONSTANT_ID", conceptService), notNullValue());
        assertThat(RegistrationAppUtils.getConcept("org.openmrs.module.registrationapp.RegistrationAppUtilsComponentTest.TEST_CONCEPT_CONSTANT_UUID", conceptService), notNullValue());
        assertThat(RegistrationAppUtils.getConcept("org.openmrs.module.registrationapp.RegistrationAppUtilsComponentTest.TEST_CONCEPT_CONSTANT_MAPPING", conceptService), notNullValue());
    }

    @Test
    @Verifies(value = "should return null otherwise", method = "getConcept(String)")
    public void getConcept_shouldReturnNullOtherwise() throws Exception {
        String id = null;
        Assert.assertNull(RegistrationAppUtils.getConcept(id, conceptService));

        id = "";
        Assert.assertNull(RegistrationAppUtils.getConcept(id, conceptService));

        id = "100000";//not exist in the standardTestData
        Assert.assertNull(RegistrationAppUtils.getConcept(id, conceptService));

        id = "ASDFASDFEAF";//random string
        Assert.assertNull(RegistrationAppUtils.getConcept(id, conceptService));

        id = ":"; //mapping style
        Assert.assertNull(RegistrationAppUtils.getConcept(id, conceptService));

        id = "-";//uuid style
        Assert.assertNull(RegistrationAppUtils.getConcept(id, conceptService));
    }

    @Test
    @Verifies(value = "should find a concept by its mapping with a space in between", method = "getConcept(String)")
    public void getConcept_shouldFindAConceptByItsMappingWithASpaceInBetween() throws Exception {
        String id = "Some Standardized Terminology: WGT234";
        Concept cpt = RegistrationAppUtils.getConcept(id, conceptService);
        Assert.assertEquals(5089, cpt.getId().intValue());
    }

    @Test
    @Verifies(value = "shoud return true valid uuid format", method = "isValidUuidFormat(String)")
    public void isValidUuidFormat_shouldReturnTrueIfNotValidUuidFormat() throws Exception {
        Assert.assertTrue(RegistrationAppUtils.isValidUuidFormat("1000AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // 36 characters
        Assert.assertTrue(RegistrationAppUtils.isValidUuidFormat("1000AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // 38 characters
    }

    @Test
    @Verifies(value = "shoud return false if not valid uuid format", method = "isValidUuidFormat(String)")
    public void isValidUuidFormat_shouldReturnFalseIfNotValidUuidFormat() throws Exception {
        Assert.assertFalse(RegistrationAppUtils.isValidUuidFormat("afasdfasd")); // less than 36 characters
        Assert.assertFalse(RegistrationAppUtils.isValidUuidFormat("012345678901234567890123456789012345678")); // more than 38 characters
        Assert.assertFalse(RegistrationAppUtils.isValidUuidFormat("1000AAAAAA AAAAAAAAA AAAAAAAAAA AAAA")); // includes whitespace
        Assert.assertFalse(RegistrationAppUtils.isValidUuidFormat("1000AAAAAA.AAAAAAAAA.AAAAAAAAAA.AAAA")); // contains periods
    }


}
