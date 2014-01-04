package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SurveyConfigRepresentationTest {
    private SurveyConfigRepresentation classUnderTest;

    @Test
    public void test_equals_success() {
        classUnderTest = new SurveyConfigRepresentation();

        assertFalse(classUnderTest.equals(null));
        assertFalse(classUnderTest.equals("someString"));
    }
}
