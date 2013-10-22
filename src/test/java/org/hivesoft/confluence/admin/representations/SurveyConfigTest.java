package org.hivesoft.confluence.admin.representations;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SurveyConfigTest {
    private SurveyConfig classUnderTest;

    @Test
    public void test_equals_success() {
        classUnderTest = new SurveyConfig();

        assertFalse(classUnderTest.equals(null));
        assertFalse(classUnderTest.equals("someString"));
    }
}
