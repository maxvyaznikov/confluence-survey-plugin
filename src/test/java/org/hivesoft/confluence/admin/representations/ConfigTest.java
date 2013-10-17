package org.hivesoft.confluence.admin.representations;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ConfigTest {
    private Config classUnderTest;

    @Test
    public void test_equals_success() {
        classUnderTest = new Config();

        assertFalse(classUnderTest.equals(null));
        assertFalse(classUnderTest.equals("someString"));
    }
}
